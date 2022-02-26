package response;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.IOException;

public class ResponseHandler
{
    private BufferedWriter out = null;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResponseHandler(BufferedWriter out)
    {
        this.out = out;
    }

    public void reply(Object object)
    {
        try
        {
            final String output = objectMapper.writeValueAsString(object);
            out.write(output);
            out.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
