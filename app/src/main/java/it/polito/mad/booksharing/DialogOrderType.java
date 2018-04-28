package it.polito.mad.booksharing;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class DialogOrderType extends DialogFragment implements View.OnClickListener {

    Context context;
    private TextView tvDistance, tvRating;

    public DialogOrderType(){

    }

    public void setContext(Context context){
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment+
        View v = inflater.inflate(R.layout.activity_dialog_order_type, container, false);
        tvDistance = (TextView) v.findViewById(R.id.distance);
        tvRating = (TextView) v.findViewById(R.id.rating);

        tvDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "SI", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        tvRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "NO", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        getDialog().getWindow().setGravity(Gravity.BOTTOM | Gravity.RIGHT);
        return v;
    }

    @Override
    public void onClick(View v) {

    }
}
