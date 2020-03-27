package com.nlscan.android.appcopy;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends BaseExpandableListAdapter {

    private List<TitleInfo> titleInfoList;
    private Context mContext;

    public MyAdapter(List<TitleInfo> list, Context context) {
        this.titleInfoList = list;
        mContext = context;
    }

    //组数
    @Override
    public int getGroupCount() {
        return titleInfoList.size();
    }

    //子数
    @Override
    public int getChildrenCount(int groupPosition) {
        return titleInfoList.get(groupPosition).getContentList().size();
    }

    //组的对象
    @Override
    public Object getGroup(int groupPosition) {
        return titleInfoList.get(groupPosition);
    }

    //子的对象
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return titleInfoList.get(groupPosition).getContentList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    //当子条目ID相同时是否复用
    @Override
    public boolean hasStableIds() {
        return true;
    }


    //  is Expandad 展开列表
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = View.inflate(mContext, R.layout.groups, null);
        TextView textView = convertView.findViewById(R.id.textGroup);
        textView.setText(titleInfoList.get(groupPosition).getTitleName());
//        if (isExpanded) {
//            textView.setTextColor(Color.GREEN);
//        } else {
//            textView.setTextColor(Color.BLACK);
//        }
        return convertView;
    }

    //isLastChild 子条目内容
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder2 holder2;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.childs, null);
            holder2 = new ViewHolder2(convertView);
            convertView.setTag(holder2);
        } else {
            holder2 = (ViewHolder2) convertView.getTag();
        }
        ContentInfo contentInfo = titleInfoList.get(groupPosition).getContentList().get(childPosition);
        holder2.tv_name.setText(contentInfo.getAppName());
        holder2.iv_icon.setImageDrawable(contentInfo.getIcon());
        return convertView;
    }

    // 子条目是否可以被点击/选中/选择
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    class ViewHolder2 {
        private TextView tv_name;
        private ImageView iv_icon;

        public ViewHolder2(View view) {
            tv_name = view.findViewById(R.id.itemTitle);
            iv_icon = view.findViewById(R.id.itemImage);
        }
    }

//    @Override
//    public void onGroupExpanded(int groupPosition) {
//        super.onGroupExpanded(groupPosition);
//        Log.d("", "onGroupExpanded() called with: groupPosition = [" + groupPosition + "]");
//        if (listenter!=null){
//            //判断是否已经打开列表的位置
//            listenter.onGroupExpanded(groupPosition);
//        }
//    }

    /**
     * 设置判断是否点击多个组对象的监听
     */
//    public void setOnGroupExPanded(OnGroupExpanded listenter){
//        this.listenter=listenter;
//    }
//    interface OnGroupExpanded{
//        void onGroupExpanded(int groupPostion);
//    }
//    OnGroupExpanded listenter;



}
