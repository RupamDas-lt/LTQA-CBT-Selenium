package DTOs.Others;

import lombok.Data;

import java.util.ArrayList;

@Data public class SeleniumVersionsDTO {
  private String os;
  private ArrayList<String> resolution;
  private ArrayList<String> selenium_version;
}