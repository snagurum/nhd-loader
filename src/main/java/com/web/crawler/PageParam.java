package com.web.crawler;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor 
public class PageParam  implements Serializable{
    String name;
    String startString;
    String endString; 
}
