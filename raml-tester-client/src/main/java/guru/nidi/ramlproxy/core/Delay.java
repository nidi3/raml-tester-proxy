package guru.nidi.ramlproxy.core;

/**
 *
 */
public interface Delay {
    void delay();

    String asCli();

    Delay NONE = new Delay() {
        @Override
        public void delay() {

        }

        @Override
        public String asCli() {
            return "0-0";
        }
    };
}
