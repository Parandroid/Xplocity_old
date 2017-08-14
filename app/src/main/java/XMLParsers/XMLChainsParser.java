package XMLParsers;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import Classes.Chain;

/**
 * Created by dmitry on 02.08.17.
 */

public class XMLChainsParser extends XMLAbstractParser {
    // We don't use namespaces
    private static final String ns = null;

    public ArrayList<Chain> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readChains(parser);
        } finally {
            in.close();
        }
    }



    private ArrayList<Chain> readChains(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Chain> chains = new ArrayList<Chain>();

        parser.require(XmlPullParser.START_TAG, ns, "User");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("Chain")) {
                chains.add(readChain(parser));
            } else {
                skip(parser);
            }
        }
        return chains;
    }

    private Chain readChain(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Chain");
        int id = Integer.parseInt(parser.getAttributeValue(null, "id"));
        String date = null;
        int loc_cnt_explored = 0;
        int loc_cnt_total = 0;


        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("date")) {
                date = readText(parser);
            } else if (name.equals("Locations")) {
                    int[] locs = readLocationsCount(parser);
                    loc_cnt_explored = locs[0];
                    loc_cnt_total = locs[1];
            } else {
                skip(parser);
            }
        }
        return new Chain(id, date, loc_cnt_explored, loc_cnt_total);
    }


    private int[] readLocationsCount(XmlPullParser parser) throws XmlPullParserException, IOException {
        int loc_cnt_explored = 0;
        int loc_cnt_total = 0;

        parser.require(XmlPullParser.START_TAG, ns, "Locations");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("explored")) {
                loc_cnt_explored = Integer.parseInt(readText(parser));
            } else if (name.equals("total")) {
                loc_cnt_total = Integer.parseInt(readText(parser));
            } else {
                skip(parser);
            }
        }

        return new int[] {loc_cnt_explored, loc_cnt_total};
    }


}