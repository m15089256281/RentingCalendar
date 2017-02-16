package com.yao.rentingcalendar;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Yao on 2016/9/7 0007.
 */
public class RentingCalendarAdapter extends RecyclerView.Adapter<RentingCalendarAdapter.ViewHolder> implements View.OnClickListener {

    public List<CalendarBean> list;

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_DAY = 1;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
    private boolean isShowBefore;
    private CalendarBean checkInDate, checkOutDate;
    private List<CalendarBean> selectCalendarBean = new ArrayList<>();
    private OnCheckOutDateListener mOnCheckOutDateListener;

    public RentingCalendarAdapter(int months) {
        int current = 0;
        list = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        while (current < months) {
            if (calendar.get(Calendar.DAY_OF_MONTH) == calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                current++;
            }
            if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                list.add(new CalendarBean(calendar).setHeader(true));
                for (int i = 1; i < calendar.get(Calendar.DAY_OF_WEEK); i++) {
                    list.add(new CalendarBean(null));
                }
            }
            list.add(new CalendarBean(calendar));
            calendar.add(Calendar.DATE, 1);
        }
        isShowBefore = true;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case TYPE_HEADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_type_header, parent, false);
                break;
            case TYPE_DAY:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_type_day, parent, false);
                break;
        }

        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CalendarBean calendarBean = list.get(position);
        holder.itemView.setTag(position);
        if (getItemViewType(position) == TYPE_HEADER) {
            holder.tvYearMonth.setText(simpleDateFormat.format(calendarBean.getCalendar().getTime()));
        } else {
            if (calendarBean.getCalendar() == null) {
                holder.itemView.setVisibility(View.INVISIBLE);
            } else {
                holder.itemView.setVisibility(View.VISIBLE);
                holder.itemView.setOnClickListener(this);
                setDayView(holder, calendarBean);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).isHeader() ? TYPE_HEADER : TYPE_DAY;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {   // 布局是GridLayoutManager所管理
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) manager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    // 如果是Header、Footer的对象则占据spanCount的位置，否则就只占用1个位置
                    return isHeader(position) ? gridLayoutManager.getSpanCount() : 1;
                }
            });
        }
    }

    private boolean isHeader(int position) {
        return list.get(position).isHeader();
    }

    public boolean isShowBefore() {
        return isShowBefore;
    }

    public RentingCalendarAdapter setShowBefore(boolean showBefore) {
        if (isShowBefore != showBefore) {
            Calendar calendar = Calendar.getInstance();
            int day4month = calendar.get(Calendar.DAY_OF_MONTH);
            int day4week = calendar.get(Calendar.DAY_OF_WEEK);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            int first4week = calendar.get(Calendar.DAY_OF_WEEK);
            if (showBefore) {
                for (int i = 1; i < day4month; i++) {
                    calendar.set(Calendar.DAY_OF_MONTH, i);
                    if (i == 1) {
                        for (int n = 1; n < day4week; n++) {
                            list.remove(i);
                        }
                        for (int n = 1; n < first4week; n++) {
                            list.add(i, new CalendarBean(null));
                        }
                    }
                    list.add(first4week + i - 1, new CalendarBean(calendar));
                }
            } else {
                for (int i = 0; i < list.size(); i++) {
                    CalendarBean bean = list.get(i);
                    if (bean.compareToNow == 0) {
                        for (int n = 1; n < day4week; n++) {
                            list.add(i, new CalendarBean(null));
                        }
                        break;
                    }
                    if (bean.isHeader()) continue;
                    list.remove(i--);
                }

            }
        }
        isShowBefore = showBefore;
        notifyDataSetChanged();
        return this;
    }

    private void setDayView(ViewHolder viewHolder, CalendarBean calendarBean) {

        int dayColor = 0;
        int statusColor = 0;
        int backgroundColor = 0;
        boolean isClickable = false;
        String dayStr;
        String statusStr = null;

        //未租未选
        if (!calendarBean.isChecked() && !calendarBean.isLease()) {
            dayColor = 0xff333333;
            statusColor = 0xff333333;
            backgroundColor = 0xffffffff;
            statusStr = "￥" + calendarBean.getMoney();
            isClickable = true;
        }
        //已租未选
        else if (!calendarBean.isChecked() && calendarBean.isLease()) {
            dayColor = 0xff333333;
            statusColor = 0xff999999;
            backgroundColor = 0xff666666;
            statusStr = "已租";
            isClickable = false;
        }
        //未租已选
        else if (calendarBean.isChecked() && !calendarBean.isLease()) {
            dayColor = 0xffffffff;
            statusColor = 0xffffffff;
            backgroundColor = 0xfffe914e;
            statusStr = "￥" + calendarBean.getMoney();
            isClickable = true;
        }
        //已租已选
        else if (calendarBean.isChecked() && calendarBean.isLease()) {
            dayColor = 0xff333333;
            statusColor = 0xff999999;
            backgroundColor = 0xffffffff;
            statusStr = "已租";
            isClickable = false;
        }


        dayStr = calendarBean.getCalendar().get(Calendar.DAY_OF_MONTH) + "";

        if (calendarBean.getCompareToNow() == 0) {
            dayStr = "今天";
            dayColor = 0xfffe914e;
            if (calendarBean.isChecked())
                dayColor = 0xffffffff;
        }

        if (calendarBean.compareTo(checkInDate) == 0) {
            backgroundColor = 0xffff711b;
            statusStr = "入住";
        }
        if (calendarBean.compareTo(checkOutDate) == 0) {
            backgroundColor = 0xffff711b;
            statusStr = "退房";
        }

        viewHolder.tvDay.setTextColor(dayColor);
        viewHolder.tvDay.setText(dayStr);
        viewHolder.tvStatus.setTextColor(statusColor);
        viewHolder.tvStatus.setText(statusStr);
        viewHolder.itemView.setBackgroundColor(backgroundColor);
        viewHolder.itemView.setClickable(isClickable);
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        CalendarBean calendarBean = list.get(position);
        if (calendarBean.getCompareToNow() < 0) {
            return;
        }
        if (checkOutDate == null && checkInDate != null && calendarBean.compareTo(checkInDate) > 0) {
            checkOutDate = calendarBean;
            boolean selecting = false;
            for (CalendarBean bean : list) {
                if (bean.isHeader() || bean.getCalendar() == null) continue;
                if (bean.compareTo(checkInDate) == 0) selecting = true;
                if (selecting) {
                    bean.setChecked(true);
                    selectCalendarBean.add(bean);
                    if (bean.isLease)
                        checkOutDate = bean;
                }
                if (bean.compareTo(checkOutDate) == 0)
                    selecting = false;
            }
            for (CalendarBean bean : selectCalendarBean) {
                Log.i("Yao", bean.getCalendar().getTime().toString());
            }
            if (mOnCheckOutDateListener != null) {
                mOnCheckOutDateListener.onCheckOutDate(selectCalendarBean);
            }
        } else {
            if (calendarBean.isLease()) return;
//            if (!isPriceCanSelect && calendarBean.money == 0) return;
            checkInDate = calendarBean;
            checkOutDate = null;
            for (CalendarBean bean : selectCalendarBean) bean.setChecked(false);
            selectCalendarBean.removeAll(selectCalendarBean);
            selectCalendarBean.add(calendarBean);
            calendarBean.setChecked(true);
        }
        notifyDataSetChanged();

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvDay, tvStatus, tvYearMonth;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDay = (TextView) itemView.findViewById(R.id.tv_day);
            tvStatus = (TextView) itemView.findViewById(R.id.tv_status);
            tvYearMonth = (TextView) itemView.findViewById(R.id.tv_year_month);
        }
    }

    public OnCheckOutDateListener getmOnCheckOutDateListener() {
        return mOnCheckOutDateListener;
    }

    public RentingCalendarAdapter setmOnCheckOutDateListener(OnCheckOutDateListener mOnCheckOutDateListener) {
        this.mOnCheckOutDateListener = mOnCheckOutDateListener;
        return this;
    }

    public interface OnCheckOutDateListener {
        void onCheckOutDate(List<CalendarBean> selectSet);
    }

    public class CalendarBean {
        private boolean isHeader = false;
        private boolean isLease = false;
        private boolean isChecked = false;
        private int compareToNow = -1;
        private int money;
        private Calendar calendar;

        public CalendarBean(Calendar calendar) {
            if (calendar == null) return;
            Calendar c = Calendar.getInstance();
            timeZeroing(calendar);
            timeZeroing(c);
            compareToNow = calendar.compareTo(c);
            c.setTime(calendar.getTime());
            this.calendar = c;
        }

        public int getMoney() {
            return money;
        }

        public CalendarBean setMoney(int money) {
            this.money = money;
            return this;
        }

        public boolean isLease() {
            return isLease;
        }

        public void setLease(boolean lease) {
            isLease = lease;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }

        public int getCompareToNow() {
            return compareToNow;
        }

        public boolean isHeader() {
            return isHeader;
        }

        public CalendarBean setHeader(boolean header) {
            isHeader = header;
            return this;
        }

        public Calendar getCalendar() {
            return calendar;
        }

        public CalendarBean setCalendar(Calendar calendar) {
            this.calendar = calendar;
            return this;
        }

        public int compareTo(CalendarBean calendarBean) {
            if (calendarBean == null) return -1;
            return calendar.compareTo(timeZeroing(calendarBean.getCalendar()));
        }

        public int compareTo(Calendar calendar) {
            if (calendar == null) return -1;
            return this.calendar.compareTo(timeZeroing(calendar));
        }
    }

    private static Calendar timeZeroing(Calendar calendar) {
        if (calendar != null) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }
        return calendar;
    }

}
