package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类，定时处理订单状态
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 支付超时订单处理
     * 对于下单后超过15分钟仍未支付的订单自动修改状态为[已取消]
     */
    @Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
    public void processTimeoutOrder() {
        log.info("开始进行支付超时订单处理:{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        // select * from orders where status = ? and order_time < (当前时间 - 15分钟)
        List<Orders> ordersList = orderMapper.getByStatusAndOrdertimeLT(Orders.PENDING_PAYMENT, time);
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders order : ordersList) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时，自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }

    }

    /**
     * 派送中状态的订单处理
     * 对于一直处于派送中状态的订单，自动修改状态为[已完成]
     */
    @Scheduled(cron = "0 0 1 * * ?")
    // 每天凌晨1点执行一次
    public void processDeliveryOrder() {
        log.info("开始进行未完成订单状态处理:{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusHours(-1);
        List<Orders> ordersList = orderMapper.getByStatusAndOrdertimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders order : ordersList) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }

    }
}
