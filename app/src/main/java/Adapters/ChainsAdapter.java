package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.dmitry.xplocity.R;

import java.util.ArrayList;

import Classes.Chain;

/**
 * Created by dmitry on 02.08.17.
 */

public class ChainsAdapter extends ArrayAdapter<Chain> {
    public ChainsAdapter(Context context, ArrayList<Chain> chains) {
        super(context, 0, chains);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Chain chain = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chain_list_item, parent, false);
        }
        // Lookup view for data population
        TextView date = (TextView) convertView.findViewById(R.id.date);
        TextView locations_explored = (TextView) convertView.findViewById(R.id.locations_explored);
        TextView locations_total = (TextView) convertView.findViewById(R.id.locations_total);
        // Populate the data into the template view using the data object
        date.setText(chain.date);
        locations_explored.setText(Integer.toString(chain.loc_cnt_explored));
        locations_total.setText(Integer.toString(chain.loc_cnt_total));
        // Return the completed view to render on screen
        return convertView;
    }
}
