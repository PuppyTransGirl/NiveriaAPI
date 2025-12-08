package toutouchien.niveriaapi.utils;

public class Direction {
    public enum Default {
        UP,
        LEFT,
        RIGHT,
        DOWN;
    }

    public enum Cardinal {
        NORTH(0, -1),
        WEST(-1, 0),
        EAST(1, 0),
        SOUTH(0, 1);

        private final int x, z;

        Cardinal(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public int x() {
            return x;
        }

        public int z() {
            return z;
        }
    }
}
