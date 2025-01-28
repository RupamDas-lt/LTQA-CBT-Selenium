package factory;

public record Locator(LocatorTypes type, String value) {
  @Override
  public String toString() {
    return "Locator[type=" + type + ", value=" + value + "]";
  }
}