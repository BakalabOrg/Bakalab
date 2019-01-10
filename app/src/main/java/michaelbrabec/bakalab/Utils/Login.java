package michaelbrabec.bakalab.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import michaelbrabec.bakalab.Interfaces.Callback;
import michaelbrabec.bakalab.ItemClasses.LoginResponse;
import michaelbrabec.bakalab.Utils.BakaTools;

public class Login extends AsyncTask<String, Void, LoginResponse> {

    private Callback callback;

    public Login (Callback callback) {
        this.callback = callback;
    }

    private String getWebContent(URL url) throws IOException {

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();
        BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder();
        String line;

        while ((line = rd.readLine()) != null) {
                content.append(line).append("\n");
        }

        return content.toString();

    }

    private static String getValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }

    @Override
    protected LoginResponse doInBackground(String... params) {

        String jmeno = params[1];
        String heslo = params[2];

        URL url;
        try {
            url = new URL(params[0] + "/login.aspx?gethx=" + jmeno);
        } catch (MalformedURLException e) {
            return new LoginResponse(false, "Server nenalezen");
        }

        String serverResponse;
        try {
             serverResponse = getWebContent(url);
        } catch (IOException e) {
            return new LoginResponse(false, "Nelze se spojit se serverem");
        }

        String tokenBase;
        String generatedToken;

        try {

            XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myParser = xmlFactoryObject.newPullParser();
            InputStream is = new ByteArrayInputStream(serverResponse.getBytes("UTF-8"));
            myParser.setInput(is, null);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);

            Element element=doc.getDocumentElement();
            element.normalize();

            String response = getValue("res", element);

            switch (response) {
                case "02":
                    return new LoginResponse(false, "Uživatel nenalezen");
                case "01":
                    //Generating SHA-512 Base64 hash of the password here
                    String hashPasswd = getValue("salt", element) + getValue("ikod", element) + getValue("typ", element) + heslo;
                    hashPasswd = BakaTools.getSha512(hashPasswd);

                    //We still to generate the token though
                    tokenBase = "*login*" + jmeno + "*pwd*" + hashPasswd + "*sgn*ANDR";
                    //continue

                    generatedToken = BakaTools.generateToken(tokenBase);
                    break;
                default:
                    return new LoginResponse(false, "Neznámá chyba");
            }

            url = new URL(params[0] + "/login.aspx?hx=" + generatedToken + "&pm=login");
            serverResponse = getWebContent(url);

            is = new ByteArrayInputStream(serverResponse.getBytes("UTF-8"));
            myParser.setInput(is, null);

             doc = dBuilder.parse(is);

            element = doc.getDocumentElement();
            element.normalize();

            String verifyResult = getValue("result", element);

            if(verifyResult.equals("-1")) {

                return new LoginResponse(false, "Špatné heslo");
            }

            return new LoginResponse(true, new String[]{
                    getValue("jmeno", element),
                    getValue("skola", element),
                    getValue("trida", element),
                    getValue("rocnik", element),
                    getValue("moduly", element),
                    getValue("typ", element),
                    getValue("strtyp", element),
                    tokenBase,
                    params[0]
            });

        } catch (XmlPullParserException | ParserConfigurationException
                | IOException | SAXException | NoSuchAlgorithmException e){
            return new LoginResponse(false,"Neznámá chyba");
        }
    }

    @Override
    protected void onPostExecute(LoginResponse result) {
        super.onPostExecute(result);
        callback.onCallbackFinish(result);
    }
}
