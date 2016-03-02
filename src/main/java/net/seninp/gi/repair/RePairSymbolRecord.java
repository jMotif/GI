package net.seninp.gi.repair;

public class RePairSymbolRecord {

  private RePairSymbol payload;

  private RePairSymbolRecord next;
  private RePairSymbolRecord prev;

  public RePairSymbolRecord(RePairSymbol symbol) {
    this.payload = symbol;
  }

  public RePairSymbol getPayload() {
    return payload;
  }

  public void setNext(RePairSymbolRecord sr) {
    this.next = sr;
  }

  public void setPrevious(RePairSymbolRecord sr) {
    this.prev = sr;
  }

  public RePairSymbolRecord getNext() {
    return this.next;
  }

  public int getIndex() {
    return this.payload.getStringPosition();
  }

  public RePairSymbolRecord getPrevious() {
    return this.prev;
  }

  public String toString() {
    if (null == this.payload) {
      return "null";
    }
    return this.payload.toString();
  }
}
