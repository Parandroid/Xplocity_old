package XMLParsers;

import android.location.Location;
import android.util.Log;
import android.util.Xml;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

import Classes.Chain;
import Classes.ChainLocation;

/**
 * Created by dmitry on 02.08.17.
 */

public class XMLChainParser extends XMLAbstractParser {
    // We don't use namespaces
    private static final String ns = null;
    private Chain chain;

    public Chain parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readChain(parser);
        }
        finally {
            in.close();
        }
    }


    private Chain readChain(XmlPullParser parser) throws XmlPullParserException, IOException {
        chain = new Chain();

        parser.require(XmlPullParser.START_TAG, ns, "Chain");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Locations")) {
                readLocations(parser);
            } else if (name.equals("Route")) {
                chain.route = Chain.string_to_route(readText(parser));
            } else {
                skip(parser);
            }
        }
        return chain;
    }


    private void readLocations(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, ns, "Locations");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Location")) {
                ChainLocation loc = readLocation(parser);
                chain.locations.add(loc);
            } else {
                skip(parser);
            }
        }
    }



    private ChainLocation readLocation(XmlPullParser parser) throws XmlPullParserException, IOException {
        ChainLocation loc = new ChainLocation();
        parser.require(XmlPullParser.START_TAG, ns, "Location");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("explored")) {
                loc.explored = Integer.parseInt(readText(parser)) == 1 ? true : false;
            } else if (name.equals("name")) {
                loc.name = readText(parser);
            } else if (name.equals("description")) {
                loc.description = readText(parser);
            } else if (name.equals("address")) {
                loc.address = readText(parser);
            } else if (name.equals("Position")) {
                loc.position = readPosition(parser);
            } else {
                skip(parser);
            }
        }

        return loc;
    }


    private LatLng readPosition(XmlPullParser parser) throws XmlPullParserException, IOException {
        double lat = 0d;
        double lon = 0d;
        parser.require(XmlPullParser.START_TAG, ns, "Position");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("latitude")) {
                lat = Double.parseDouble(readText(parser));
            } else if (name.equals("longitude")) {
                lon = Double.parseDouble(readText(parser));
            } else {
                skip(parser);
            }


        }
        LatLng pos = new LatLng(lat, lon);
        return pos;
    }


}