import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author: hushi
 * @create: 2019/03/14
 */
public class TestTimer {

    private String str = "";

    TimerTask task = new TimerTask()
    {
        public void run()
        {
            if( str.equals("") )
            {
                System.out.println( "you input nothing. exit..." );
                System.exit( 0 );
            }
        }
    };

    public void getInput() throws Exception
    {
        Timer timer = new Timer();
        timer.schedule( task, 10*1000 );

        System.out.println( "Input a string within 10 seconds: " );
        BufferedReader in = new BufferedReader(
                new InputStreamReader( System.in ) );
        str = in.readLine();

        timer.cancel();
        System.out.println( "you have entered: "+ str );
    }

    public static void main( String[] args )
    {
        try
        {
            (new TestTimer()).getInput();
        }
        catch( Exception e )
        {
            System.out.println( e );
        }
        System.out.println( "main exit..." );
    }
}
