package com.wuzhen.rural.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;

@Entity
@Table(name = "barrage")
@JsonIgnoreProperties({"handeler","hibernateLazyInitializer"})
public class Barrage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private int id;
  private String danmu;


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getDanmu() {
    return danmu;
  }

  public void setDanmu(String danmu) {
    this.danmu = danmu;
  }

}
