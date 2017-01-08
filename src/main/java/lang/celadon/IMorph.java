package lang.celadon;

import java.util.List;

/**
 * Created by Tommy Ettinger on 1/7/2017.
 */
public interface IMorph {
    int morph(final List<Token> tokens, int start, int end);
}
