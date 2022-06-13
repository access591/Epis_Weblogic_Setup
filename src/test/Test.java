package test;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.io.*;
public class Test {
  public static void main(String arg[]) throws IOException {
	  String command = "https://vsms.minavo.in/api/singlesms.php";
	  
	  URL url = new URL("https://vsms.minavo.in/api/singlesms.php?auth_key=766988e5-8f25-4aad-b131-7c68726dcd2f&mobilenumber=7986733231&message=Dear%20XXXXKH.%20DILIPKUMAR%20SINGH%2C%20your%20request%20for%20Missing%20Credit%20for%20the%0Amonth%20of%20032021%2C%20has%20not%20been%20approved.%20%20Log%20in%20ID%20M302%20PasswordVbpD%40252%5Csmswebservicesagaemanipur.cag.gov.in%20-%0A%20%20%20%20%20Principal%20Accountant%20General(AE)%2C%20Manipur.&sid=SRAOPF&mtype=N&template_id=1207163885827736819");
      URLConnection con = url.openConnection();
      
      InputStream is = new URL(url.toString()).openStream();
      
      
    try {
      String[] finalCommand;
      if (isWindows()) {
        finalCommand = new String[4];
        // Use the appropriate path for your windows version.
        //finalCommand[0] = "C:\\winnt\\system32\\cmd.exe";    // Windows NT/2000
       // finalCommand[0] = "C:\\windows\\system32\\cmd.exe";    // Windows XP/2003
        //finalCommand[0] = "C:\\windows\\syswow64\\cmd.exe";  // Windows 64-bit
  //      finalCommand[0]="C:"+File.separator+"Windows"+File.separator+"System32"+File.separator+"cmd.exe";
    finalCommand[0] = "C:/windows/system32/cmd.exe";  
        finalCommand[1] = "/y";
        finalCommand[2] = "/c";
        finalCommand[3] = command;
      }
      else {
        finalCommand = new String[3];
        finalCommand[0] = "/bin/sh";
        finalCommand[1] = "-c";
        finalCommand[2] = command;
      }
  
      //final Process pr = Runtime.getRuntime().exec(finalCommand);
     // pr.waitFor();

      new Thread(new Runnable(){
        public void run() {
          BufferedReader br_in = null;
          try {
            br_in = new BufferedReader(new InputStreamReader(is));
            String buff = null;
            while ((buff = br_in.readLine()) != null) {
              System.out.println("Process out :" + buff);
              try {Thread.sleep(100); } catch(Exception e) {}
            }
            br_in.close();
          }
          catch (IOException ioe) {
            System.out.println("Exception caught printing process output.");
            ioe.printStackTrace();
          }
          finally {
            try {
              br_in.close();
            } catch (Exception ex) {}
          }
        }
      }).start();
  
      new Thread(new Runnable(){
        public void run() {
          BufferedReader br_err = null;
          try {
            br_err = new BufferedReader(new InputStreamReader(is));
            String buff = null;
            while ((buff = br_err.readLine()) != null) {
              System.out.println("Process err :" + buff);
              try {Thread.sleep(100); } catch(Exception e) {}
            }
            br_err.close();
          }
          catch (IOException ioe) {
            System.out.println("Exception caught printing process error.");
            ioe.printStackTrace();
          }
          finally {
            try {
              br_err.close();
            } catch (Exception ex) {}
          }
        }
      }).start();
    }
    catch (Exception ex) {
      System.out.println(ex.getLocalizedMessage());
    }
  }
  
  public static boolean isWindows() {
    if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1)
      return true;
    else
      return false;
  }
}