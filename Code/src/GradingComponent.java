import java.util.Objects;

/**
 * Represents a single grading component with a name and a weight (percentage).
 */
public class GradingComponent {
    private String name;
    private int weight; // weight as an integer percentage (0–100)

    public GradingComponent(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "GradingComponent{" +
                "name='" + name + '\'' +
                ", weight=" + weight +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GradingComponent that = (GradingComponent) o;
        return weight == that.weight &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, weight);
    }
}
