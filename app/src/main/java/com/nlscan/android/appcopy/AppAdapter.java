package com.nlscan.android.appcopy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppAdapter  extends RecyclerView.Adapter<AppAdapter.MyViewHolder>{

    private List<String> list;

    private MyClickListener myClickListener;

    public void setMyClickListener(MyClickListener myClickListener) {
        this.myClickListener=myClickListener;
    }

    public AppAdapter(List<String> list) {
        this.list=list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //绑定子视图
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycle_item, viewGroup, false);
        MyViewHolder myViewHolder=new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
        TextView textView =myViewHolder.itemView.findViewById(R.id.itemTitle);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myClickListener.setTextClickListener(i);
            }
        });
        textView.setText(list.get(i));
        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myClickListener.setOnClickListener(i);
            }
        });
        myViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                myClickListener.setOnLongClickListener(i);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    //定义视图管理器
    class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    //事件监听
    public interface MyClickListener{
        void setOnClickListener(int i);
        void setOnLongClickListener(int i);
        void setTextClickListener(int i);
    }

}
