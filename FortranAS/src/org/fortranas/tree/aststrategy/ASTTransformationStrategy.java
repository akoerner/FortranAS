import java.util.List;
import java.util.Map;

public interface ASTTransformationStrategy {
    void run(Map<String, Object> node, List<Object> rules);
}
