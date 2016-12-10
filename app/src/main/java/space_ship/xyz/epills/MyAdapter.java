package space_ship.xyz.epills;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by Nastya on 27.11.2016.
 */

//Это в итоге нигде не будет использоваться скорее всего. Но пусть пока будет
public class MyAdapter  extends BaseAdapter {
    private Pill[] listData;
    private LayoutInflater layoutInflater;

    public MyAdapter(Pill[] listData, LayoutInflater layoutInflater) {
        this.layoutInflater = layoutInflater;
        this.listData = listData;
    }

    @Override
    public int getCount() {
        return listData.length;
    }

    @Override
    public Object getItem(int position) {
        return listData[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
