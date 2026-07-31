/* Qurium - 2026 */
package org.qurium.nlquery.ai;

import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;
import org.mockito.Mockito;

/**
 * CDI alternative that replaces AiSqlService in tests.
 *
 * Using @Mock (CDI alternative) instead of @InjectMock avoids triggering
 * a Quarkus context restart, which would cause LangChain4j to eagerly
 * initialize its HTTP client and call the real AI API.
 *
 * Tests access the underlying Mockito mock via {@link #instance()} to
 * configure per-test behavior with when(...).thenReturn(...).
 */
@Mock
@ApplicationScoped
public class MockAiSqlService implements AiSqlService {

    private static final AiSqlService DELEGATE = Mockito.mock(AiSqlService.class);

    public static AiSqlService instance() {
        return DELEGATE;
    }

    public static void reset() {
        Mockito.reset(DELEGATE);
    }

    @Override
    public AiSqlResponse generateSql(String schemaJson, String question) {
        return DELEGATE.generateSql(schemaJson, question);
    }

    @Override
    public AiResultSnapshot generateResultSnapshot(String question, String result) {
        return DELEGATE.generateResultSnapshot(question, result);
    }
}
