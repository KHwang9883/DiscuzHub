package com.kidozh.discuzhub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kidozh.discuzhub.R;
import com.kidozh.discuzhub.entities.ThreadCount;

import java.util.ArrayList;
import java.util.List;


public class ThreadCountAdapter extends RecyclerView.Adapter<ThreadCountAdapter.ThreadCountHolder> {

    private Context context;
    @NonNull
    List<ThreadCount> ThreadCountList = new ArrayList<>();

    private OnRecommendBtnPressed mListener;

    @NonNull
    @Override
    public ThreadCountHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        if(context instanceof OnRecommendBtnPressed){
            mListener = (OnRecommendBtnPressed) context;
        }
        int layoutIdForListItem = R.layout.item_bbs_thread_type;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ThreadCountHolder(view);
    }

    public void setThreadCountList(@NonNull List<ThreadCount> ThreadCountList){
        int oldSize = this.ThreadCountList.size();
        this.ThreadCountList.clear();
        notifyItemRangeRemoved(0,oldSize);
        this.ThreadCountList = ThreadCountList;
        notifyItemRangeInserted(0,ThreadCountList.size());
    }

    @Override
    public void onBindViewHolder(@NonNull ThreadCountHolder holder, int position) {

        ThreadCount notification = ThreadCountList.get(position);
        if(mListener !=null){
            switch (position){
                case 0:{
                    holder.itemThreadTypeCardview.setOnClickListener(v -> {
                        mListener.onRecommend(true);
                    });
                    break;
                }
                case 1:{
                    holder.itemThreadTypeCardview.setOnClickListener(v -> {
                        mListener.onRecommend(false);
                    });
                    break;
                }
            }

        }


        if(notification.highlightColorRes == -1){
            holder.itemThreadTypeAvatar.setImageResource(notification.imageResource);
            holder.itemThreadTypeTextview.setText(notification.typeString);
        }
        else {
            holder.itemThreadTypeAvatar.setImageResource(notification.imageResource);
            holder.itemThreadTypeTextview.setText(notification.typeString);
            holder.itemThreadTypeCardview.setBackgroundColor(notification.highlightColorRes);
            holder.itemThreadTypeTextview.setTextColor(context.getColor(R.color.colorPureWhite));
            holder.itemThreadTypeAvatar.setColorFilter(context.getColor(R.color.colorPureWhite));

        }

    }

    @Override
    public int getItemCount() {
        if(ThreadCountList == null){
            return 0;
        }
        else {
            return ThreadCountList.size();
        }
    }

    public class ThreadCountHolder extends RecyclerView.ViewHolder{
        CardView itemThreadTypeCardview;
        ImageView itemThreadTypeAvatar;
        TextView itemThreadTypeTextview;

        public ThreadCountHolder(@NonNull View itemView) {
            super(itemView);
            itemThreadTypeCardview = itemView.findViewById(R.id.item_bbs_thread_type_cardview);
            itemThreadTypeAvatar = itemView.findViewById(R.id.item_bbs_thread_type_avatar);
            itemThreadTypeTextview = itemView.findViewById(R.id.item_bbs_thread_type_value);
        }
    }

    public interface OnRecommendBtnPressed{
        void onRecommend(boolean recommend);
    }


}
