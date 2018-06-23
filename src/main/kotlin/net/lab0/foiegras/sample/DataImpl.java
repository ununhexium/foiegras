package net.lab0.foiegras.sample;

/**
 * Big fat Java data class
 */
public class DataImpl implements Data {
  private String name;
  private Object reference;
  private int start;
  private int end;
  private float[] values;

  public DataImpl() {}

  public DataImpl(String name, Object reference, int start, int end, float[] values) {
    this.name = name;
    this.reference = reference;
    this.start = start;
    this.end = end;
    this.values = values;
  }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Object getReference() {
        return reference;
    }

    public void setReference(Object reference) {
        this.reference = reference;
    }

    @Override
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    @Override
    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public float[] getValues() {
        return values;
    }

    public void setValues(float [] values) {
        this.values = values;
    }
}
