/* Qurium - 2026 */
package org.qurium.nlquery.ai;

import java.util.List;
import java.util.Map;

public record ResultSnapshot(String type, Object value, List<Map<String, Object>> rows) {}
