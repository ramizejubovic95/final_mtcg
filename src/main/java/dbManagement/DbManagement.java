package dbManagement;

import PackageMackage.PackageManager;
import battle.Battle;
import card.Card;
import response.ResponseHandler;
import trading.Tradeable;
import user.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DbManagement {
    private final String dbInfo = "jdbc:postgresql://localhost:5432/postgres";
    private final String dbName = "ramiz";
    private final String dbPassword = "password";
    private ResponseHandler response;
    private Connection c = null;

    public DbManagement(){}

    public DbManagement(ResponseHandler response)
    {
        this.response = response;
    }

    public void open() throws SQLException
    {
        this.c = DriverManager.getConnection(this.dbInfo, this.dbName, this.dbPassword);
    }

    public void close() throws SQLException
    {
        this.c.close();
    }

    public boolean checkIfUserExist(User user) throws SQLException
    {
        String sql = "SELECT username FROM USERS WHERE username = ?";
        ResultSet result = null;
        this.open();

        try (PreparedStatement pstmt = this.c.prepareStatement(sql))
        {
            pstmt.setString(1, user.getUsername());
            result = pstmt.executeQuery();
            if (result.next())
            {
                this.close();
                return true;
            }
            else
            {
                this.close();
                return false;
            }
        }
        catch (SQLException throwables)
        {
            this.close();
            return false;
        }
    }

    public boolean addUser(User user) throws SQLException
    {
        String SQL = "INSERT INTO USERS(username, password, authtoken, gems, elopoints, bio, image, namehandleofuser, wins, losses, draws, totalbattles)" + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

        if (checkIfUserExist(user)) return false;

        this.open();
        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, String.valueOf(user.getPassword().hashCode()));
            pstmt.setString(3, user.getUsername() + "-mtcgToken");
            pstmt.setInt(4, 20);
            pstmt.setInt(5, 100);
            pstmt.setString(6, "I'm new lets fight!");
            pstmt.setString(7, "oOo");
            pstmt.setString(8, user.getUsername());
            pstmt.setInt(9, 0);
            pstmt.setInt(10, 0);
            pstmt.setInt(11, 0);
            pstmt.setInt(12, 0);

            pstmt.executeUpdate();
            this.close();
        }
        catch (SQLException e) { System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close(); return false; }
        catch (Exception ex) { System.out.println("User could not be saved to DB\n"); this.close(); return false; }

        this.close();
        System.out.println("User added to DB\n");
        return true;
    }

    public User passwordCheck(User user) throws SQLException
    {
        String sql = "SELECT * FROM USERS WHERE username = ? AND password = ?";
        ResultSet result = null;

        this.open();
        try (PreparedStatement pstmt = this.c.prepareStatement(sql))
        {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, String.valueOf(user.getPassword().hashCode()));
            result = pstmt.executeQuery();

            if (result.next())
            {
                User currentUser = setUserDataComingFormDbToInstance(result);
                // Gets All Cards and Sets Deck
                currentUser = getCardsByUserId(currentUser);

                this.close();
                return currentUser;
            }
            throw new SQLException();
        }
        catch (SQLException throwables) { this.close(); return null; } catch (Exception e) {
            e.printStackTrace();
            this.close();
            return null;
        }

    }

    public User login(User user) throws Exception
    {
        User currentUser;
        if (!checkIfUserExist(user)) return null;
        if ((currentUser = passwordCheck(user)) == null) return null;

        currentUser = getCardsByUserId(currentUser);
        return currentUser;
    }

    public boolean checkIfCardExists(Card card) throws SQLException
    {
        String SQL = "SELECT cardid FROM CARDS WHERE cardid = ?";
        ResultSet result = null;

        this.open();
        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setString(1, card.getCardId());
            result = pstmt.executeQuery();
            if (result.next())
            {
                this.close();
                return true;
            }
            else
            {
                this.close();
                return false;
            }
        }
        catch (SQLException throwables)
        {
            this.close();
            return true;
        }
    }

    public boolean storeCards(List<Card> cards) throws SQLException
    {
        String SQL = "INSERT INTO CARDS(cardid, cardname, damage, element, cardtype, packageid, islocked)" + "VALUES(?,?,?,?,?,?,?)";

        for (int i = 0; i < cards.size(); i++)
        {
            this.open();

            try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
            {

                // Nur unique Cards werden gespeichert, unnötig weil in Packages die selben Karten sein können
                /* if(this.checkIfCardExists(cards.get(i)))
                {
                    if (i == (cards.size() - 1))
                        break;

                    System.out.println(cards.get(i).getCardName() + " could not get saved. Skipping!\n ");
                    continue;
                }*/

                pstmt.setString(1, cards.get(i).getCardId());
                pstmt.setString(2, cards.get(i).getCardName());
                pstmt.setFloat(3, cards.get(i).getDamage());
                pstmt.setString(4, cards.get(i).getElement());
                pstmt.setString(5, cards.get(i).getCardType());
                pstmt.setInt(6, cards.get(i).getPackageId());
                pstmt.setBoolean(7, false);

                pstmt.executeUpdate();
                System.out.println("SUCCESSFULLY ADDED: " + cards.get(i).getCardName());
                this.close();
            }
            catch (SQLException e) { System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());this.close(); return false; }
            catch (Exception ex) { System.out.println("Last Card could not be saved to DB\n");this.close(); return false; }

            this.close();
        }
        this.close();
        return true;
    }

    public PackageManager createPackage() throws SQLException
    {
        PackageManager newPackage = new PackageManager();
        String SQL = "INSERT INTO PACKAGES(price, isbought)" + "VALUES(?, ?)";
        this.open();

        try (PreparedStatement pstmt = this.c.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS))
        {
            pstmt.setInt(1, newPackage.getPrice());
            pstmt.setBoolean(2, false);

            int rows = pstmt.executeUpdate();
            if(rows == 0)
                throw new Exception();

            try (ResultSet result = pstmt.getGeneratedKeys())
            {
                if(result.next())
                {
                    newPackage.setId(result.getInt("id"));
                    this.close();
                    return newPackage;
                }
                else
                {
                    System.out.println("PACKAGE COULD NOT BE ADDED TO DB!\n");
                    throw new Exception();
                }
            }
        }
        catch (SQLException e) { System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());this.close(); return null; }
        catch (Exception ex) { System.out.println("create Packs: Package could not be saved to DB\n"); this.close(); return null; }
    }

    public User buyPacks(User user) throws SQLException
    {
        String SQL = "SELECT * FROM PACKAGES WHERE isbought = ? LIMIT 1";
        ResultSet result = null;
        User currentUser = user;

        this.open();
        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setBoolean(1, false);
            result = pstmt.executeQuery();
            if (result.next())
            {
                PackageManager packageManager = new PackageManager();
                packageManager.setId(result.getInt("id"));
                setPackToBought(packageManager.getId());
                currentUser = getCardsByPackageId(currentUser, packageManager.getId());

            }
            else
            {
                // Give User gems back
                currentUser.setGems(currentUser.getGems() + 5);
            }
            this.close();
            return currentUser;
        }
        catch (SQLException e) { System.err.format("buyPacks SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close(); return user; }
        catch (Exception ex) { System.out.println("buy Packs Package could not be saved to DB\n"); this.close(); return user; }
    }

    public void setPackToBought(int id) throws SQLException
    {
        String SQL = "UPDATE PACKAGES SET isbought = ? WHERE id = ?";
        this.open();

        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setBoolean(1, true);
            pstmt.setInt(2, id);

            int rows = pstmt.executeUpdate();
            if(rows == 0)
                throw new Exception();
            this.close();
        }
        catch (SQLException e) { System.err.format("packWasBought SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close();}
        catch (Exception ex) { System.err.println("Package could not be bought to DB\n");this.close(); }
        this.close();
    }

    public User getCardsByPackageId(User user, int packageId) throws SQLException
    {
        String SQL = "SELECT * FROM CARDS WHERE packageid = ?";
        ResultSet result = null;
        User currentUser = user;
        this.open();

        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setInt(1, packageId);
            result = pstmt.executeQuery();

            while (result.next())
            {
                Card newCard = new Card();
                newCard.setId(result.getInt("id"));
                newCard.setCardId(result.getString("cardid"));
                newCard.setCardName(result.getString("cardname"));
                newCard.setDamage(result.getFloat("damage"));
                newCard.setElement(result.getString("element"));
                newCard.setCardType(result.getString("cardtype"));
                newCard.setLocked(result.getBoolean("islocked"));
                newCard.setPackageId(packageId);
                newCard.setUserid(currentUser.getId());

                setUserIdToCardToMarkOwnership(currentUser.getId(), newCard.getId());
            }
            currentUser = getCardsByUserId(currentUser);
            this.close();
            // System.out.println("NICE PACKAGE YOU GOT THERE!\n");
            return currentUser;
        }
        catch (SQLException e) { System.err.format("getCardsByPackageId SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close(); return user; }
        catch (Exception ex) { System.out.println("getCardsByPackageId PAck Package could not be saved to DB\n"); this.close(); return user; }
    }

    public void setUserIdToCardToMarkOwnership(int userId, int cardId) throws SQLException
    {
        String SQL = "UPDATE CARDS SET userid = ? WHERE id = ?";

        this.open();

        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, cardId);

            int rows = pstmt.executeUpdate();
            if(rows == 0)
                throw new Exception();

            this.close();
        }
        catch (SQLException e) { System.err.format("setUserIdToCardToMarkOwnership SQL State: %s\n%s", e.getSQLState(), e.getMessage());this.close(); }
        catch (Exception ex) { System.err.println("setUserIdToCardToMarkOwnership : Package could not be bought\n");this.close(); }
        this.close();
    }

    public User getCardsByUserId(User user) throws Exception
    {
        String SQL = "SELECT * FROM CARDS WHERE userid = ? ORDER BY damage DESC";
        ResultSet result = null;
        User currentUser = user;
        this.open();
        List<Card> newStack = new ArrayList<Card>();


        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setInt(1, user.getId());
            result = pstmt.executeQuery();

            while (result.next())
            {
                Card newCard = new Card();
                newCard.setId(result.getInt("id"));
                newCard.setCardId(result.getString("cardid"));
                newCard.setCardName(result.getString("cardname"));
                newCard.setDamage(result.getFloat("damage"));
                newCard.setElement(result.getString("element"));
                newCard.setCardType(result.getString("cardtype"));
                newCard.setPackageId(result.getInt("packageid"));
                newCard.setUserid(result.getInt("userid"));
                newCard.setLocked(result.getBoolean("islocked"));

                newStack.add(newCard);

            }

            this.close();
            currentUser.setAllCards(newStack);
            currentUser.setBestCardsFromStackToDeck();
            return currentUser;
        }
        catch (SQLException e) { System.err.format("getCardsByUserId SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close(); return user; }
        catch (Exception ex) { System.out.println("getCardsByUserId\n"); this.close(); return user; }
    }

    public void storeCurrentDeckToDb(User user) throws SQLException
    {
        String SQL = "INSERT INTO DECK(cardone, cardtwo, cardthree, cardfour, userid)" + "VALUES(?,?,?,?,?)";
        deletePreviousDeckFromDb(user);
        this.open();

        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            for (int i = 0; i < 5; i++)
            {
                if (i < 4) pstmt.setInt(i+1, user.getDeck().get(i).getId());
                if (i == 4) pstmt.setInt(i+1, user.getId());
            }

            int rows = pstmt.executeUpdate();
            if(rows == 0)
                throw new Exception();
            this.close();
        }
        catch (SQLException e) { System.err.format("KOMMT DAS VON HIER? SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close(); }
        catch (Exception ex) { System.out.println("Deck could not be saved to DB\n"); this.close();}
        this.close();
    }

    public void deletePreviousDeckFromDb(User user) throws SQLException
    {
        String SQL = "DELETE FROM DECK WHERE userid = ?";
        this.open();

        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setInt(1, user.getId());
            pstmt.executeUpdate();
            this.close();
        }
        catch (SQLException e) { System.err.format("deletePreviousDeckFromDb SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close();}
        catch (Exception ex) { System.out.println("Deck could not be deleted!\n"); this.close(); }

    }

    public User setUserDataComingFormDbToInstance(ResultSet result) throws SQLException
    {
        User currentUser = new User();
        currentUser.setId(result.getInt("id"));
        currentUser.setUsername(result.getString("username"));
        currentUser.setPassword(result.getString("password"));
        currentUser.setAuthToken(result.getString("authtoken"));
        currentUser.setGems(result.getInt("gems"));
        currentUser.setEloPoints(result.getInt("elopoints"));

        // Profiel Data
        currentUser.setNameHandleOfUser(result.getString("namehandleofuser"));
        currentUser.setImage(result.getString("image"));
        currentUser.setBio(result.getString("bio"));

        // Stats Data
        currentUser.setWins(result.getInt("wins"));
        currentUser.setLosses(result.getInt("losses"));
        currentUser.setDraws(result.getInt("draws"));
        currentUser.setTotalBattles(result.getInt("totalbattles"));

        return currentUser;
    }

    public void saveLastUserData(User user) throws SQLException
    {
        String SQL = "UPDATE USERS SET username = ?, password = ?, gems = ?, authtoken = ?, elopoints = ?, bio = ?, image = ?, namehandleofuser = ?, wins = ?, losses = ?, draws = ?, totalbattles = ?  WHERE id = ?";

        this.open();

        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setInt(3, user.getGems());
            pstmt.setString(4, user.getAuthToken());
            pstmt.setInt(5, user.getEloPoints());
            pstmt.setString(6, user.getBio());
            pstmt.setString(7, user.getImage());
            pstmt.setString(8, user.getNameHandleOfUser());
            pstmt.setInt(9, user.getWins());
            pstmt.setInt(10, user.getLosses());
            pstmt.setInt(11, user.getDraws());
            pstmt.setInt(12, user.getTotalBattles());
            pstmt.setInt(13, user.getId());

            int rows = pstmt.executeUpdate();
            if(rows == 0)
                throw new Exception();

            storeCurrentDeckToDb(user);

            if (rows == 1)
                System.out.println("User saved successfully!");
            this.close();

        }
        catch (SQLException e) { System.err.format("SAVELASTUSERDATA SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close();}
        catch (Exception ex) { System.err.println("SAVELASTUSERDATA : Package could not be bought\n"); this.close();}
    }

    public User getUserByToken(String token) throws Exception
    {
        String SQL = "SELECT * FROM USERS WHERE authtoken = ?";
        ResultSet result = null;
        User user = null;

        this.open();
        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setString(1, token);
            result = pstmt.executeQuery();

            if (result.next())
            {
                user = setUserDataComingFormDbToInstance(result);
                user = getCardsByUserId(user);
                this.close();
                return user;
            }
            else
            {
                this.close();
                return null;
            }
        }
        catch (SQLException throwables)
        {
            System.out.println("INVALID AUTHTOKEN\n");
            this.close();
            return user;
        }
    }

    public Tradeable setTradeableFromDbToInstance(ResultSet result) throws SQLException
    {
        Tradeable tradeable = new Tradeable();

        tradeable.setId(result.getInt("id"));
        tradeable.setCardIdOfTradeable(result.getString("cardid"));
        tradeable.setCardTypeOfTradeable(result.getString("cardtype"));
        tradeable.setDamageOfTradeable(result.getFloat("damage"));
        tradeable.setCurrentUserId(result.getInt("currentuserid"));
        tradeable.setCurrentUserAuthToken(result.getString("currentuserauthtoken"));
        tradeable.setOriginUserId(result.getInt("originuserid"));
        tradeable.setOriginUserAuthToken(result.getString("originauthtoken"));
        tradeable.setTradeId(result.getString("tradeid"));
        tradeable.setTraded(result.getBoolean("istraded"));

        return tradeable;
    }

    public List<Tradeable> updateTradingsHistory(User user) throws SQLException
    {
        String SQL = "SELECT * FROM TRADINGS WHERE currentuserid = ?";
        ResultSet result = null;

        this.open();
        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            List<Tradeable> updatedList = new ArrayList<Tradeable>();

            pstmt.setInt(1, user.getId());
            result = pstmt.executeQuery();
            if (!result.next())
            {
                this.close();
                return null;
            }
            while (result.next())
            {
                System.out.println(user.getId());
                Tradeable tradeableObject = setTradeableFromDbToInstance(result);
                updatedList.add(tradeableObject);
            }
            this.close();
            return updatedList;
        }
        catch (SQLException e) { System.err.format("UPDATE TRADINGHISTORY SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close(); return null; }
        catch (Exception ex) { System.err.println("UPDATE TRADINGHISTORY : Package could not be bought\n"); this.close(); return null; }
    }

    public List<User> updateScoreBoard() throws SQLException
    {
        String SQL = "SELECT * FROM USERS ORDER BY elopoints DESC";
        ResultSet result = null;

        this.open();
        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            List<User> updatedList = new ArrayList<User>();
            result = pstmt.executeQuery();
            while (result.next())
            {
                User userInScoreBoard = setUserDataComingFormDbToInstance(result);
                updatedList.add(userInScoreBoard);
            }
            this.close();
            return updatedList;
        }
         catch (SQLException throwables)
        {
            System.out.println("SOMETHING WENT WRONG WITH UPDATING SCOREBOARD");
            this.close();
            return null;
        }
    }

    public void saveNewTrade(Tradeable newTrade) throws SQLException
    {
        String SQL = "INSERT INTO TRADINGS(cardid, cardtype, currentuserid, currentuserauthtoken, originuserid, originauthtoken, tradeid, damage, istraded)" + "VALUES(?,?,?,?,?,?,?,?, ?)";

        this.open();
        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setString(1, newTrade.getCardIdOfTradeable());
            pstmt.setString(2, newTrade.getCardTypeOfTradeable());
            pstmt.setInt(3, newTrade.getCurrentUserId());
            pstmt.setString(4, newTrade.getCurrentUserAuthToken());
            pstmt.setInt(5, newTrade.getOriginUserId());
            pstmt.setString(6, newTrade.getOriginUserAuthToken());
            pstmt.setString(7, newTrade.getTradeId());
            pstmt.setFloat(8, newTrade.getDamageOfTradeable());
            pstmt.setBoolean(9, false);

            int rows = pstmt.executeUpdate();
            if(rows == 0)
                throw new Exception();

            this.close();
        }
        catch (SQLException e) { System.err.format("SaveNewTrade SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close(); }
        catch (Exception ex) { System.out.println("Deck could not be saved to DB\n"); this.close(); }
        this.close();
    }

    public String getCardIdByTradeId(String requestedTradeId) throws SQLException
    {
        String SQL = "SELECT cardid FROM TRADINGS WHERE tradeid = ?";
        ResultSet result = null;

        this.open();
        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setString(1, requestedTradeId);
            result = pstmt.executeQuery();
            if (result.next())
            {
                this.close();
                return result.getString("cardid");
            }
            this.close();
            return null;
        }
        catch (SQLException e) { System.err.format("getCardIdByTradeId SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close(); return null; }
        catch (Exception ex) { System.err.println("getCardIdByTradeId : Package could not be bought\n"); this.close(); return null; }
    }

    public Tradeable getTradeableByTradeId(String requestedTradeId) throws SQLException
    {
        String SQL = "SELECT * FROM TRADINGS WHERE tradeid = ?";
        ResultSet result = null;

        this.open();
        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setString(1, requestedTradeId);
            result = pstmt.executeQuery();
            System.out.println(requestedTradeId);
            if (result.next())
            {
                Tradeable trade = setTradeableFromDbToInstance(result);
                this.close();
                return trade;
            }
            this.close();
            return null;
        }
        catch (SQLException e) { System.err.format("getCardIdByTradeId SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close(); return null; }
        catch (Exception ex) { System.err.println("getCardIdByTradeId : Package could not be bought\n"); this.close(); return null; }

    }

    public boolean updateTradeFromDbByTradeId(String requestedTradeId) throws SQLException
    {
        String SQL = "UPDATE TRADINGS SET istraded = ? WHERE tradeid = ?";
        this.open();

        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            unlockCard(getTradeableByTradeId(requestedTradeId).getCardIdOfTradeable());
            pstmt.setBoolean(1, true);
            pstmt.setString(2, requestedTradeId);
            pstmt.executeUpdate();
            System.out.println("Trade deleted successfully");
            this.close();
            return true;
        }
        catch (SQLException e) { System.err.format("deleteTradeFromDbByTradeId SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close(); return false; }
        catch (Exception ex) { System.err.println("deleteTradeFromDbByTradeId \n");this.close(); return false; }
    }

    public boolean setCardToLocked(String cardIdOfTradeable) throws SQLException
    {
        String SQL = "UPDATE CARDS SET islocked = ? WHERE cardid = ?";

        this.open();
        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setBoolean(1, true);
            pstmt.setString(2, cardIdOfTradeable);

            int rows = pstmt.executeUpdate();

            if (rows == 0)
            {
                return false;
            }
            System.out.println("Card is locked!");
            return true;
        }
        catch (SQLException e) { System.err.format("SAVELASTUSERDATA SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close(); return false; }
        catch (Exception ex) { System.err.println("SAVELASTUSERDATA : Package could not be bought\n"); this.close(); return false; }
    }

    public boolean unlockCard(String cardid) throws SQLException
    {
        String SQL = "UPDATE CARDS SET islocked = ? WHERE cardid = ?";

        this.open();
        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setBoolean(1, false);
            pstmt.setString(2, cardid);

            int rows = pstmt.executeUpdate();
            if(rows == 0)
                return false;

            if (rows == 1)
            {
                System.out.println("Card is locked!");
                return true;
            }
            this.close();
            return true;
        }
        catch (SQLException e) { System.err.format("SAVELASTUSERDATA SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close(); return false; }
        catch (Exception ex) { System.err.println("SAVELASTUSERDATA : Package could not be bought\n"); this.close(); return false; }
    }

    public Card getCardsByCardId(String cardId) throws SQLException
    {
        String SQL = "SELECT * FROM Cards WHERE cardid = ?";
        ResultSet result = null;

        this.open();
        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setString(1, cardId);
            result = pstmt.executeQuery();

            if (result.next())
            {
                Card returnCard = new Card();

                returnCard.setId(result.getInt("id"));
                returnCard.setCardId(result.getString("cardid"));
                returnCard.setCardName(result.getString("cardname"));
                returnCard.setDamage(result.getFloat("damage"));
                returnCard.setElement(result.getString("element"));
                returnCard.setCardType(result.getString("cardtype"));
                returnCard.setPackageId(result.getInt("packageid"));
                returnCard.setUserid(result.getInt("userid"));
                returnCard.setLocked(result.getBoolean("islocked"));

                this.close();
                return returnCard;
            }
            else
            {
                this.close();
                return null;
            }
        }
        catch (SQLException e) { System.err.format("SAVELASTUSERDATA SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close(); return null; }
        catch (Exception ex) { System.err.println("SAVELASTUSERDATA : Package could not be bought\n");this.close(); return null; }
    }

    public boolean switchOwnerOfCard(Card cardOfSeller, Card cardOfBuyer) throws SQLException
    {
        String SQL = "UPDATE CARDS SET userid = ?, islocked = ? WHERE id = ?";
        this.open();

        Card zwischenSpeicher = cardOfSeller;

        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setInt(1, cardOfBuyer.getUserid());
            pstmt.setBoolean(2, false);
            pstmt.setInt(3, cardOfSeller.getId());

            int rows = pstmt.executeUpdate();
            if(rows == 0)
                throw new Exception();

            if (rows == 1)
                System.out.println("Card 1 was traded!");

            this.close();
        }
        catch (SQLException e) { System.err.format("switchOwnerOfCard1 SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close(); return false; }
        catch (Exception ex) { System.err.println("switchOwnerOfCard : Package could not be bought\n"); this.close(); return false; }

        this.open();
        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setInt(1, zwischenSpeicher.getUserid());
            pstmt.setBoolean(2, false);
            pstmt.setInt(3, cardOfBuyer.getId());

            int rows = pstmt.executeUpdate();
            if(rows == 0)
                throw new Exception();
            if (rows == 1)
                System.out.println("Card2 is traded!");

            unlockCard(cardOfSeller.getCardId());
            updateTradeFromDbByCardId(cardOfSeller);
            this.close();
            return true;
        }
        catch (SQLException e) { System.err.format("switchOwnerOfCard2 SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close(); return false;}
        catch (Exception ex) { System.err.println("switchOwnerOfCard : Package could not be bought\n"); this.close(); return false;}
    }

    public boolean updateTradeFromDbByCardId(Card card) throws SQLException
    {
        String SQL = "UPDATE TRADINGS SET istraded = ?, currentuserid = ? WHERE cardid = ?";
        this.open();

        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setBoolean(1, true);
            pstmt.setInt(2, card.getUserid());
            pstmt.setString(3, card.getCardId());
            int rows = pstmt.executeUpdate();

            System.out.println("ROWS: " + rows);
            if (rows == 0)
            {
                this.close();
                return false;
            }

            System.out.println("Trade updated successfully");
            this.close();
            return true;
        }
        catch (SQLException e) { System.err.format("deleteTradeFromDbByTradeId SQL State: %s\n%s", e.getSQLState(), e.getMessage()); this.close(); return false; }
        catch (Exception ex) { this.close(); return false; }
    }

    public Battle setBattleDataFromDbtoInstance(ResultSet result) throws SQLException
    {
        Battle foundBattle = new Battle();

        foundBattle.setBattleId(result.getInt("battleid"));
        foundBattle.setFighter1(result.getInt("fighterone"));
        foundBattle.setFighter2(result.getInt("fightertwo"));
        foundBattle.setWinner(result.getInt("winner"));
        foundBattle.setLoser(result.getInt("loser"));
        foundBattle.setDraws(result.getBoolean("draws"));

        return foundBattle;
    }

    public Battle searchForBattle() throws SQLException
    {
        String SQL = "SELECT * FROM BATTLES WHERE fighterone = ? OR fightertwo = ? LIMIT 1";
        ResultSet result;
        Battle foundBattle = null;

        this.open();

        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setInt(1,0);
            pstmt.setInt(2,0);

            result = pstmt.executeQuery();

            if (result.next())
            {
                foundBattle = setBattleDataFromDbtoInstance(result);
                this.close();
                return foundBattle;
            }
            else
            {
                this.close();
                return foundBattle;
            }
        }
        catch (SQLException e) { System.err.format("SAVELASTUSERDATA SQL State: %s\n%s", e.getSQLState(), e.getMessage());this.close(); return null; }
        catch (Exception ex) { System.err.println("SAVELASTUSERDATA : Package could not be bought\n");this.close(); return null; }
    }

    public Battle createBattle(Battle battle) throws Exception
    {
        String SQL = "INSERT INTO BATTLES(fighterone, fightertwo, winner, loser, draws, isfinished)" + "VALUES(?, ?, ?, ?, ?, ?)";
        Battle newBattle = battle;

        this.open();
        try (PreparedStatement pstmt = this.c.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS))
        {
            pstmt.setInt(1, battle.getFighter1());
            pstmt.setInt(2, battle.getFighter2());
            pstmt.setInt(3, 0);
            pstmt.setInt(4, 0);
            pstmt.setBoolean(5, false);
            pstmt.setBoolean(6, false);

            int rows = pstmt.executeUpdate();

            if (rows == 0)
                throw new Exception();

            try (ResultSet result = pstmt.getGeneratedKeys())
            {
                if (result.next())
                {
                    newBattle.setBattleId(result.getInt("battleid"));
                    this.close();
                    return newBattle;
                }
                else
                {
                    this.close();
                    return newBattle;
                }

            }
            catch (Exception e)
            {
                System.out.println("SOME PROBLEMS");
                this.close();
                return battle;
            }
        }
    }

    public boolean saveBattleData(Battle battle) throws SQLException
    {
        String SQL = "UPDATE BATTLES SET fighterone = ?, fightertwo = ?, winner = ?, loser = ?, draws = ?, isfinished = ? WHERE battleid = ?";

        this.open();
        try (PreparedStatement pstmt = this.c.prepareStatement(SQL))
        {
            pstmt.setInt(1, battle.getFighter1());
            pstmt.setInt(2, battle.getFighter2());
            pstmt.setInt(3, battle.getWinner());
            pstmt.setInt(4, battle.getLoser());
            pstmt.setBoolean(5, battle.isDraws());
            pstmt.setBoolean(6, battle.isFinished());
            pstmt.setInt(7, battle.getBattleId());

            int rows = pstmt.executeUpdate();
            if(rows == 0)
            {
                this.close();
                return false;
            }


            if (rows == 1)
            {
                this.close();
                return true;
            }

            this.close();
            return true;
        }
        catch (SQLException e) { System.err.format("saveBattleData SQL State: %s\n%s", e.getSQLState(), e.getMessage());  this.close(); return false; }
        catch (Exception ex) { System.err.println("saveBattleData :\n" + ex.getStackTrace()); this.close(); return false; }
    }
}