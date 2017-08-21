package lang.celadon.old;

import squidpony.squidmath.RandomnessSource;
import squidpony.squidmath.StatefulRNG;

import java.util.List;

/**
 * Created by Tommy Ettinger on 2/28/2017.
 */
public class Wrappers {
    public static class _StatefulRNG extends StatefulRNG implements ICallByName
    {
        public _StatefulRNG() {
            super();
        }

        public _StatefulRNG(RandomnessSource random) {
            super(random);
        }

        /**
         * Seeded constructor uses LightRNG, which is of high quality, but low period (which rarely matters for games),
         * and has good speed and tiny state size.
         *
         * @param seed any long
         */
        public _StatefulRNG(long seed) {
            super(seed);
        }

        /**
         * String-seeded constructor uses the hash of the String as a seed for LightRNG, which is of high quality, but low
         * period (which rarely matters for games), and has good speed and tiny state size.
         *
         * @param seedString any String; may be null
         */
        public _StatefulRNG(String seedString) {
            super(seedString);
        }

        @Override
        public Token call(Token name, List<Token> args) {
            return Methods.valueOf(name.asString()).call(this, args);
        }
        enum Methods {
            nextLong {
                public Token call(StatefulRNG me, List<Token> args) {
                    return args.isEmpty()
                            ? Token.stable(me.nextLong())
                            : Token.stable(me.nextLong(args.get(0).asLong()));
                }
            }, nextInt {
                public Token call(StatefulRNG me, List<Token> args) {
                    return args.isEmpty()
                            ? Token.stable(me.nextInt())
                            : Token.stable(me.nextIntHasty(args.get(0).asInt()));
                }
            }, nextDouble {
                public Token call(StatefulRNG me, List<Token> args) {
                    return args.isEmpty()
                            ? Token.stable(me.nextDouble())
                            : Token.stable(me.nextDouble(args.get(0).asDouble()));
                }
            }, between {
                public Token call(StatefulRNG me, List<Token> args) {
                    return args.get(0).floating() || args.get(1).floating()
                            ? Token.stable(me.between(args.get(0).asDouble(), args.get(1).asDouble()))
                            : Token.stable(me.between(args.get(0).asLong(), args.get(1).asLong()));
                }
            }, getState {
                public Token call(StatefulRNG me, List<Token> args) {
                    return Token.stable(me.getState());
                }
            }, setState {
                public Token call(StatefulRNG me, List<Token> args) {
                    me.setState(args.get(0).asLong());
                    return Token.stable(me);
                }
            }, next {
                public Token call(StatefulRNG me, List<Token> args) {
                    return Token.stable(me.next(args.get(0).asInt()));
                }
            }, randomOrdering {
                public Token call(StatefulRNG me, List<Token> args) {
                    return Token.stable(me.randomOrdering(args.get(0).asInt()));
                }
            };

            public abstract Token call(StatefulRNG me, List<Token> args);

        }
    }
}
