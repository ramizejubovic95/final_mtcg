package Requests;

import PackageMackage.PackageManager;
import battle.Battle;
import card.Card;
import card.CardManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import dbManagement.DbManagement;
import response.ResponseHandler;
import stats.Score;
import trading.Tradeable;
import trading.TradingService;
import user.User;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.*;

import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class ReqHandler
{
    ObjectMapper oMap = new ObjectMapper();
    DbManagement db;
    ResponseHandler response;
    User user1 = null;
    private static BlockingQueue<User> blockingQueue = new ArrayBlockingQueue<>(10);

    public ReqHandler(ResponseHandler response)
    {
        this.response = response;
        this.db = new DbManagement(this.response);
    }

    public boolean doesRequestContainAuthToken(Request req)
    {
        if (req.getAuthorization() == null)
        {
            System.out.println("NO AUTHTOKEN.Connection refused");
            return false;
        }
        return true;
    }

    public boolean isAdmin(String authToken)
    {
        return "admin-mtcgToken".equals(authToken);
    }

    public User switchLoggedInUserToRequestingUser(Request req) throws SQLException
    {
        if (!doesRequestContainAuthToken(req))
        {
            this.response.reply("No Authtoken");
            return null;
        }

        return user1 = db.getUserByToken(req.getAuthorization());
    }

    public boolean isLoggedIn(Request req)
    {
        if (this.user1 == null)
        {
            response.reply("Not Logged In");
            return false;
        }

        boolean isLoggedIn = user1.getAuthToken().equals(req.getAuthorization());

        response.reply("Logged In");
        return isLoggedIn;
    }

    public User mapUser(Request req)
    {
        User currentUser = user1;
        try
        {
            currentUser = oMap.readValue(req.getContent(), User.class);
        }
        catch (JsonMappingException e) { e.printStackTrace(); return null; }
        catch (JsonProcessingException e) { System.out.println("SOMETHING WENT WRONG WITH USER\n"); return null; }

        return currentUser;
    }

    public Tradeable mapTradeAble(Request req, User user)
    {
        Tradeable newTrade = null;
        try
        {
            newTrade = oMap.readValue(req.getContent(), Tradeable.class);

            newTrade.setCurrentUserId(user1.getId());
            newTrade.setCurrentUserAuthToken(user1.getAuthToken());
            newTrade.setOriginUserId(user1.getId());
            newTrade.setOriginUserAuthToken(user1.getAuthToken());

            return newTrade;
        }
        catch (JsonMappingException e) { e.printStackTrace(); return null; }
        catch (JsonProcessingException e) { System.out.println("SOMETHING WENT WRONG WITH USER\n"); return null; }

    }

    public User updateCurrentUser(Request req) throws JsonProcessingException
    {
        // Turn String into --> [{}]
        User currentUser = oMap.readValue(req.getContent(), User.class);

        user1.setNameHandleOfUser(currentUser.getNameHandleOfUser());
        user1.setImage(currentUser.getImage());
        user1.setBio(currentUser.getBio());

        return user1;
    }

    public boolean isAuthMatchingRoute(Request req) throws SQLException
    {
        String[] splittedRouteStrings = req.getRoute().split("/users/");
        String getNameOutOfRoute = splittedRouteStrings[1];

        return req.getAuthorization().contains(getNameOutOfRoute);
    }



    public void handle(Request req) throws SQLException, JsonProcessingException
    {
        switch (req.getMethod()) {
            case "GET" -> {
                if ("/cards".equals(req.getRoute()))
                {
                    if ((user1 = switchLoggedInUserToRequestingUser(req)) == null) break;

                    this.response.reply(user1.getAuthToken());
                    user1.print();
                }
                if ("/deck".equals(req.getRoute()))
                {
                    if ((user1 = switchLoggedInUserToRequestingUser(req)) == null) break;

                    this.response.reply("HERE SHOULD BE THE DECK PRINTED " + user1.getAuthToken());
                    user1.printDeck();
                }
                if ("/deck?format=plain".equals(req.getRoute()))
                {
                    if ((user1 = switchLoggedInUserToRequestingUser(req)) == null) break;

                    System.out.println(req);
                    System.out.println("TODO\n");
                }
                if ("/users/kienboec".equals(req.getRoute()))
                {
                    if (!isAuthMatchingRoute(req))
                    {
                        this.response.reply("NOT AUTHORIZED!");
                        break;
                    }
                    if ((user1 = switchLoggedInUserToRequestingUser(req)) == null) break;

                    this.response.reply("HERE SHOULD BE USER DATA PRINTED OF " + user1.getUsername());
                    user1.userPrintsHimself();
                }
                if ("/users/altenhof".equals(req.getRoute()))
                {
                    if (!isAuthMatchingRoute(req))
                    {
                        this.response.reply("NOT AUTHORIZED!");
                        break;
                    }

                    if ((user1 = switchLoggedInUserToRequestingUser(req)) == null) break;

                    this.response.reply("HERE SHOULD BE USER DATA PRINTED OF " + user1.getUsername());
                    user1.userPrintsHimself();
                }
                if ("/score".equals(req.getRoute()))
                {
                    Score scoreBoard = new Score();
                    scoreBoard.updateScoreBoard();
                    scoreBoard.print();

                    this.response.reply("SCOREBOARD: " + scoreBoard.getScoreBoard().get(1).getAuthToken());
                }
                if("/stats".equals(req.getRoute()))
                {
                    if ((user1 = switchLoggedInUserToRequestingUser(req)) == null) break;

                    this.response.reply("PRINT STATS OF : " + user1.getWins());
                    user1.showStats();
                }
                if("/tradings".equals(req.getRoute()))
                {
                    if ((user1 = switchLoggedInUserToRequestingUser(req)) == null) break;

                    user1.updateTradingsHistory();
                    this.response.reply("Tradings History FROM: " + user1.getUsername());
                    user1.printTradingsHistory();
                }
            }
            case "POST" -> {
                if ("/users".equals(req.getRoute()))
                {
                    if ((user1 = this.mapUser(req)) != null)
                    {
                        if(db.addUser(user1))
                        {
                            this.response.reply("User added to Database successfully!");
                        }
                        else
                        {
                            this.response.reply("User exists");
                        }
                    }
                }
                if ("/sessions".equals(req.getRoute()))
                {
                    User currentUser = null;
                    if ((currentUser = this.mapUser(req)) != null)
                    {
                        if ((this.user1 = db.login(currentUser)) != null)
                        {
                            this.response.reply("Welcome " + user1.getUsername());
                            System.out.println("Welcome " + user1.getUsername());
                            break;
                        }
                        this.response.reply("Either username or password is wrong.");
                        System.out.println("Either username or password is wrong.");
                        break;
                    } else
                    {
                        System.out.println("COULD NOT MAP STUFF");
                        break;
                    }
                }
                if ("/packages".equals(req.getRoute()))
                {
                    if (!isAdmin(req.getAuthorization()))
                    {
                        response.reply("You have no admin rights, ya");
                        System.out.println("You have no admin rights, ya");
                        break;
                    }
                    if ((user1 = switchLoggedInUserToRequestingUser(req)) == null) break;

                    // Create New Package and store to DB and gets ID;
                    PackageManager newPackage = db.createPackage();

                    if (newPackage == null)
                        System.out.println("RIP\n");

                    // Turn String into --> [{}]
                    TypeFactory typeFactory = oMap.getTypeFactory();
                    List<Card> someCards = oMap.readValue(req.getContent(), typeFactory.constructCollectionType(List.class, Card.class));

                    for (int i = 0; i < someCards.toArray().length; i++) {
                        CardManager cardManager = new CardManager();
                        someCards.set(i, cardManager.setElement(someCards.get(i)));
                        someCards.set(i, cardManager.setType(someCards.get(i)));
                        someCards.set(i, cardManager.setPackageId(someCards.get(i), newPackage.getId()));
                    }

                    db.storeCards(someCards);
                    this.response.reply("New Cards are added to the shop by Admin");
                }
                if ("/transactions/packages".equals(req.getRoute()))
                {
                    if ((user1 = switchLoggedInUserToRequestingUser(req)) == null) break;

                    if (user1.getGems() < 5)
                        this.response.reply("NOT ENOUGH COINS BROKE FELLA");
                    else
                    {
                        User userToCheckIfAllCardsSizeChanged = new User();
                        userToCheckIfAllCardsSizeChanged.setAllCards(user1.getAllCards());
                        System.out.println(userToCheckIfAllCardsSizeChanged.getAllCards().size());

                        user1 = db.buyPacks(user1);
                        user1.setGems(user1.getGems() - 5);
                        user1.setBestCardsFromStackToDeck();
                        db.saveLastUserData(user1);
                        user1 = db.getCardsByUserId(user1);
                        System.out.println("OTHER USER DECK SSITE : " + userToCheckIfAllCardsSizeChanged.getAllCards().size());
                        System.out.println("ACTUAL DECK SIZE : " + user1.getAllCards().size() + " \n\n\n");

                        this.response.reply(" " + user1.getAuthToken() + " Add All Cards Showing");
                    }
                }
                if ("/battles".equals(req.getRoute()))
                {
                    if ((user1 = switchLoggedInUserToRequestingUser(req)) == null) break;

                    if (blockingQueue.contains(user1))
                    {
                        this.response.reply("YOU ARE ALREADY WAITING FOR A FIGHT!");
                        break;
                    }

                    blockingQueue.add(user1);
                    boolean isReadyToFight = blockingQueue.toArray().length == 2;

                    if (isReadyToFight)
                    {
                        try
                        {
                            User fighter1 = blockingQueue.take();
                            User fighter2 = blockingQueue.take();

                            Battle battle = new Battle(fighter1, fighter2, this.response);
                            battle = db.createBattle(battle);

                            if (battle.getFighter1() != 0 && battle.getFighter2() != 0)
                            {
                                System.out.println("LETS FIGHT WITH BATTLE ID: " + battle.getBattleId());
                                battle = battle.fight(fighter1, fighter2);
                                db.saveBattleData(battle);
                            }
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }
                    else
                    {
                        this.response.reply("You are the first one in the room. Please wait for the battle!");
                    }
                }
                if ("/tradings".equals(req.getRoute()))
                {
                    if ((user1 = switchLoggedInUserToRequestingUser(req)) == null) break;

                    Tradeable newTrade = mapTradeAble(req, user1);
                    if (!user1.isUserOwnerOfCard(newTrade.getCardIdOfTradeable()))
                    {
                        this.response.reply("You are not the owner of this card!");
                        break;
                    }
                    if (user1.isCardInDeck(newTrade.getCardIdOfTradeable()))
                    {
                        this.response.reply("You cannot trade cards in your deck");
                        break;
                    }
                    if (user1.isCardLocked(newTrade.getCardIdOfTradeable()))
                    {
                        this.response.reply("Card is locked. It seems that it is already in the Marketplace");
                        break;
                    }

                    db.saveNewTrade(newTrade);
                    db.setCardToLocked(newTrade.getCardIdOfTradeable());

                    user1 = db.getCardsByUserId(user1);
                    this.response.reply("YOUR CARD IS READY TO TRADE!");
                }
                if (req.getRoute().contains("/tradings/"))
                {
                    if ((user1 = switchLoggedInUserToRequestingUser(req)) == null) break;

                    String[] getTradeIdFromRoute = req.getRoute().split("/tradings/");
                    String requestedTradeId = getTradeIdFromRoute[1].replaceAll("\"", "");

                    // Check ob user besitzer der karte ist
                    Tradeable tradeToCheck = db.getTradeableByTradeId(requestedTradeId);
                    if (tradeToCheck == null)
                    {
                        this.response.reply("TRADE DOES NOT EXIST!");
                        break;
                    }
                    if (tradeToCheck.getCurrentUserId() == user1.getId())
                    {
                        this.response.reply("YOU CANNOT TRADE WITH YOURSELF");
                        break;
                    }

                    TradingService marketplace = new TradingService();
                    String cardOfferFromRequester = req.getContent().replaceAll("\"", "").replaceAll("\r", "").replaceAll("\n", "");

                    boolean cardCanBeTraded = user1.isUserOwnerOfCard(cardOfferFromRequester);

                    if (cardCanBeTraded)
                    {
                        Card seller = db.getCardsByCardId(tradeToCheck.getCardIdOfTradeable());
                        Card buyer = db.getCardsByCardId(cardOfferFromRequester);


                        if (marketplace.tradeCards(seller, buyer))
                        {
                            this.response.reply("Trade was successfull");
                            break;
                        }
                        else
                        {
                            this.response.reply("Requirements are not met");
                        }
                    }
                    else
                    {
                        this.response.reply("Trade not possible");
                    }
                }
            }
            case "PUT" -> {
                if ("/deck".equals(req.getRoute()))
                {
                    if ((user1 = switchLoggedInUserToRequestingUser(req)) == null) break;

                    String[] result = Arrays.stream(req.getContent().split(",")).toArray(String[]::new);
                    for (int i = 0; i < result.length; i++) {
                        result[i] = result[i].replaceAll("\"", "").replace("[", "").replace("]", "").replace("\n", "").replaceAll("\\s", "");
                    }

                    if (result.length < 4)
                    {
                        System.out.println("Not enough cards choosen to put in deck");
                        this.response.reply("Not enough cards choosen to put in deck");
                        break;
                    }

                    // isOwner checkt ob einzelne Karte in newDeck kommt
                    // isNewDeckValid checkt ob alle Karten Valid waren
                    boolean isOwner = false;
                    boolean isNewDeckValid = true;
                    List<Card> newDeck = new ArrayList<Card>();
                    for (String s : result)
                    {
                        isNewDeckValid = false;
                        for (int j = 0; j < user1.getAllCards().size(); j++)
                        {
                            if (s.equals(user1.getAllCards().get(j).getCardId()))
                            {
                                isOwner = true;
                                newDeck.add(user1.getAllCards().get(j));
                            }
                            if (isOwner) isNewDeckValid = true;
                        }
                    }

                    if (!isNewDeckValid)
                    {
                        System.out.println(("You do not own some of these cards"));
                        this.response.reply("You do not own some of these cards");
                        break;
                    }

                    user1.setDeck(newDeck);
                    db.storeCurrentDeckToDb(user1);
                    user1.printDeck();
                    this.response.reply("YOUR DECK IS SET TO FIGHT!");
                }
                if ("/users/kienboec".equals(req.getRoute()))
                {
                    if (!isAuthMatchingRoute(req))
                    {
                        this.response.reply("NOT AUTHORIZED!");
                        break;
                    }
                    if ((user1 = switchLoggedInUserToRequestingUser(req)) == null) break;

                    user1 = updateCurrentUser(req);
                    db.saveLastUserData(user1);
                    this.response.reply("USER DATA UPDATED!");
                }
                if ("/users/altenhof".equals(req.getRoute()))
                {
                    if (!isAuthMatchingRoute(req))
                    {
                        this.response.reply("NOT AUTHORIZED!");
                        break;
                    }
                    if ((user1 = switchLoggedInUserToRequestingUser(req)) == null) break;

                    user1 = updateCurrentUser(req);
                    db.saveLastUserData(user1);
                    this.response.reply("USER DATA UPDATED!");
                }
            }
            case "DELETE" -> {
                if (req.getRoute().contains("/tradings/"))
                {
                    if ((user1 = switchLoggedInUserToRequestingUser(req)) == null) break;

                    String[] getTradeIdFromRoute = req.getRoute().split("/tradings/");
                    String requestedTradeId = getTradeIdFromRoute[1].replaceAll("\"", "");

                    if(!user1.isUserOwnerOfCard(db.getCardIdByTradeId(requestedTradeId))) break;

                    db.deleteTradeFromDbByTradeId(requestedTradeId);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + req.getMethod());
        }
    }
}
