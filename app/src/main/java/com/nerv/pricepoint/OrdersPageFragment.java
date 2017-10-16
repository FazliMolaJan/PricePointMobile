package com.nerv.pricepoint;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by NERV on 11.10.2017.
 */

public class OrdersPageFragment extends CustomFragment {

    private static class OrderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView orderNumber;
        private TextView retail;
        private TextView address;
        private TextView startDate;
        private TextView endDate;
        private TextView tasksCount;
        private TextView completeTasksCount;
        private TextView noGoodsTasksCount;
        private TextView toEditTasksCount;
        private TextView fotosCount;

        public Order order;

        private MainActivity main;

        public void init(MainActivity main) {
            this.main = main;
        }

        public OrderHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            orderNumber = (TextView) itemView.findViewById(R.id.orderNumber);
            retail = (TextView) itemView.findViewById(R.id.retail);
            address = (TextView) itemView.findViewById(R.id.address);
            startDate = (TextView) itemView.findViewById(R.id.startDate);
            endDate = (TextView) itemView.findViewById(R.id.endDate);

            tasksCount = (TextView) itemView.findViewById(R.id.tasksCount);
            completeTasksCount = (TextView) itemView.findViewById(R.id.completeTasksCount);
            noGoodsTasksCount = (TextView) itemView.findViewById(R.id.noGoodsTasksCount);
            toEditTasksCount = (TextView) itemView.findViewById(R.id.toEditTasksCount);
            fotosCount = (TextView) itemView.findViewById(R.id.fotosCount);
        }

        public void setOrder(Order order) {
            this.order = order;

            orderNumber.setText(String.valueOf(order.orderId) + " " + order.mark);
            retail.setText(order.retail + ", " + order.city);
            address.setText(order.address);
            startDate.setText(Utils.dateToString(order.startDate));
            endDate.setText(Utils.dateToString(order.endDate));

            tasksCount.setText(String.valueOf(order.tasks.size()));

            order.computeOrderInfo();

            completeTasksCount.setText(String.valueOf(order.completedTasks));
            noGoodsTasksCount.setText(String.valueOf(order.noGoodsTasks));
            toEditTasksCount.setText(String.valueOf(order.toEditTasks));
            fotosCount.setText(String.valueOf(order.photos));
        }

        @Override
        public void onClick(View v) {
            main.getDatabaseManager().selectedOrder = order;
            main.getPageController().setPage(PageController.Page.ORDER);
        }
    }

    public static class OrderRecyclerAdapter extends RecyclerView.Adapter<OrderHolder> {
        private MainActivity main;

        public OrderRecyclerAdapter(MainActivity main) {
            this.main = main;
        }

        @Override
        public OrderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
            OrderHolder orderHolder = new OrderHolder(view);

            orderHolder.init(main);

            return orderHolder;
        }

        @Override
        public void onBindViewHolder(OrderHolder holder, int position) {
            holder.setOrder(main.getDatabaseManager().orders.get(position));
        }

        @Override
        public int getItemCount() {
            return main.getDatabaseManager().orders.size();
        }
    }

    private View view;

    private MainActivity main;
    private RecyclerView ordersRV;
    private OrderRecyclerAdapter orderRecyclerAdapter;

    @Override
    public void init(MainActivity main) {
        this.main = main;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.orders_page_layout, null);

        ordersRV = (RecyclerView) view.findViewById(R.id.ordersRV);
        ordersRV.setLayoutManager(new LinearLayoutManager(main, LinearLayoutManager.VERTICAL, false));
        orderRecyclerAdapter = new OrderRecyclerAdapter(main);
        ordersRV.setAdapter(orderRecyclerAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ordersRV = null;
        orderRecyclerAdapter = null;
    }
}

