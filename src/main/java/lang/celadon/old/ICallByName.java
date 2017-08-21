package lang.celadon.old;

import java.util.List;

/**
 * Created by Tommy Ettinger on 2/27/2017.
 */
public interface ICallByName {
    Token call(Token name, List<Token> args);
}
