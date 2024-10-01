package com.example.ai_customizing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppInfoAdapter extends RecyclerView.Adapter<AppInfoAdapter.ViewHolder> {

    private final List<AppInfo> appInfoList;

    public AppInfoAdapter(List<AppInfo> appInfoList) {
        this.appInfoList = appInfoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo appInfo = appInfoList.get(position);
        holder.appIcon.setImageDrawable(appInfo.getIconResId());
        holder.appName.setText(appInfo.getName());
        holder.appInfo.setText(appInfo.getInfo());
    }

    @Override
    public int getItemCount() {
        return appInfoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView appInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.app_icon);
            appName = itemView.findViewById(R.id.app_name);
            appInfo = itemView.findViewById(R.id.app_info);
        }
    }

    public void updateTextAtPosition(int position, String newText) {
        if (position >= 0 && position < appInfoList.size()) {
            appInfoList.get(position).setInfo(newText); // 예를 들어 usageTime을 업데이트
            notifyItemChanged(position); // RecyclerView에 변경 알림
        }
    }
}
