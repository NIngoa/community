package com.nowcoder.community.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface DateService {

    public void recordUV(String ip);

    public long calculateUV(LocalDate startDay, LocalDate endDay);

    public void recordDAU(int userId);

    public long calculateDAU(LocalDate startDay, LocalDate endDay);
}
