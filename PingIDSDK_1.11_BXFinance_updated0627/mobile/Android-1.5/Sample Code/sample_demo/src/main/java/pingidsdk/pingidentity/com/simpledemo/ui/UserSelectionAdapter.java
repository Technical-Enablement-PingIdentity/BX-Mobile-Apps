package pingidsdk.pingidentity.com.simpledemo.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import pingidsdk.pingidentity.com.simpledemo.R;
import pingidsdk.pingidentity.com.simpledemo.models.User;

/**
 * Created by PingIdentity on 11/6/18.
 */

public class UserSelectionAdapter extends ArrayAdapter<User> implements View.OnClickListener {

    private ArrayList<User> users;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        IosTextView name;
    }

    public UserSelectionAdapter(@NonNull Context context, ArrayList<User> data) {
        super(context, R.layout.user_row_item, data);
        this.users = data;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = getItem(position);
        ViewHolder viewHolder;

        final View result;
        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.user_row_item, parent, false);
            viewHolder.name = (IosTextView) convertView.findViewById(R.id.username_choice);
            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }
        if(user.getFirstname()!=null && !user.getFirstname().isEmpty()){
            viewHolder.name.setText(user.getFirstname().concat(" ").concat(user.getLastname()));
        }else {
            viewHolder.name.setText(user.getUsername());
        }
        return convertView;
    }

    @Override
    public void onClick(View v) {

    }
}
