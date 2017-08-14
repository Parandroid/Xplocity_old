package XMLParsers;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import Classes.Chain;
import Classes.LocationCategory;

/**
 * Created by dmitry on 04.08.17.
 */


public class XMLLocationCategoryParser extends XMLAbstractParser {
    // We don't use namespaces
    private static final String ns = null;

    public ArrayList<LocationCategory> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readLocationCategories(parser);
        } finally {
            in.close();
        }
    }


    private ArrayList<LocationCategory> readLocationCategories(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<LocationCategory> location_categories = new ArrayList<LocationCategory>();

        parser.require(XmlPullParser.START_TAG, ns, "location_categories");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("location_category")) {
                location_categories.add(readLocationCategory(parser));
            } else {
                skip(parser);
            }
        }
        return location_categories;
    }

    private LocationCategory readLocationCategory(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "location_category");

        int id = 0;
        String category_name = null;
        String description = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("id")) {
                id = Integer.parseInt(readText(parser));
            } else if (name.equals("name")) {
                category_name = readText(parser);
            } else if (name.equals("description")) {
                description = readText(parser);

            } else {
                skip(parser);
            }
        }
        return new LocationCategory(id, category_name, description);
    }
}
