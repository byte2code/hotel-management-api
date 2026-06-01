package com.cn.hotelDemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@EnableCaching
public class HotelDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelDemoApplication.class, args);
	}

}
