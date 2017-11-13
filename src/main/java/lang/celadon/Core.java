package lang.celadon;

/**
 * Created by Tommy Ettinger on 8/27/2017.
 */
public class Core {
    public static final Cel yes = new Cel("true", true);
    public static final Cel no = new Cel("false", false);
    public static final Cel nothing = new Cel("null", null);

    public static long asLong(Object o)
    {
        if(o instanceof Number)
            return ((Number)o).longValue();
        if(o instanceof Boolean)
            return Boolean.TRUE.equals(o) ? 1L : 0L;
        return 0L;
    }

    public static double asDouble(Object o)
    {
        if(o instanceof Number)
            return ((Number)o).doubleValue();
        if(o instanceof Boolean)
            return Boolean.TRUE.equals(o) ? 1.0 : 0.0;
        return 0.0;
    }

    public static boolean isNumeric(Object o)
    {
        return (o instanceof Number) || (o instanceof Boolean);
    }

    public static boolean isFloating(Object o)
    {
        return (o instanceof Double) || (o instanceof Float);
    }

    public static Procedural plus = new Procedural() {
        @Override
        public void run(Manager manager) {
            if(!manager.exchange.isEmpty()) {
                if (isFloating(manager.exchange.peekFirst())) {
                    double first = asDouble(manager.exchange.pollFirst());
                    if (manager.exchange.isEmpty())
                        manager.exchange.addFirst(first);
                    else
                        manager.exchange.addFirst(first + asDouble(manager.exchange.pollFirst()));
                } else {
                    long first = asLong(manager.exchange.pollFirst());
                    if (manager.exchange.isEmpty()) {
                        if (isFloating(manager.exchange.peekFirst()))
                            manager.exchange.addFirst(first + asDouble(manager.exchange.pollFirst()));
                        else
                            manager.exchange.addFirst(first + asLong(manager.exchange.pollFirst()));
                    } else
                        manager.exchange.addFirst(first);
                }
            }
        }
    };

}
