package lang.celadon;

/**
 * Interface for function-like objects that can be called when given a Manager to exchange parameters and results with.
 * Created by Tommy Ettinger on 8/27/2017.
 */
public interface Procedural {
    Cel run(Cel left, Cel right);
}
