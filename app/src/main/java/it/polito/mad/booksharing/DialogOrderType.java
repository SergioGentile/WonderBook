package it.polito.mad.booksharing;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DialogOrderType extends BottomSheetDialogFragment {
    Context context;
    private BottomSheetListener mListener;
    private TextView tvDistance, tvRating, tvRecent, tvCity;

    public DialogOrderType() {

    }

    public void setContext(Context context) {
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment+
        View v = inflater.inflate(R.layout.activity_dialog_order_type, container, false);
        tvDistance = v.findViewById(R.id.distance);
        tvRating = v.findViewById(R.id.rating);
        tvRecent = v.findViewById(R.id.recent);
        tvCity = v.findViewById(R.id.myCity);

        tvDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onButtonClicked(0);
                dismiss();
            }
        });

        tvRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onButtonClicked(1);
                dismiss();
            }
        });

        tvRecent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onButtonClicked(2);
                dismiss();
            }
        });
        tvCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onButtonClicked(3);
                dismiss();
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BottomSheetListener");
        }
    }

    public interface BottomSheetListener {
        void onButtonClicked(int position);
    }

}
