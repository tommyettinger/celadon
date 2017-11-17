package lang.celadon;

import squidpony.squidmath.OrderedSet;

/**
 * Created by Tommy Ettinger on 11/16/2017.
 */
public abstract class Operator implements Procedural {
    public final int precedence;
    public final boolean rightAssociative;

    public static final OrderedSet<Operator> registry = new OrderedSet<>(32);

    public Operator()
    {
        precedence = 0;
        rightAssociative = false;
        registry.add(this);
    }

    public Operator(int precedence)
    {
        this.precedence = precedence;
        rightAssociative = false;
        registry.add(this);
    }

    public Operator(int precedence, boolean rightAssociative)
    {
        this.precedence = precedence;
        this.rightAssociative = rightAssociative;
        registry.add(this);
    }

}
