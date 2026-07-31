/* Qurium - 2026 */
package org.qurium.nlquery.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface AiSqlService {

    @SystemMessage(
            """
            You are a SQL expert. Given a database schema in JSON format and a user question,
            generate a valid read-only SQL SELECT query that answers the question.
            Respond ONLY with a valid JSON object in this exact format:
            {
                "sql": "<the SQL query>",
                 "explanation": "<plain English explanation>"
            }
            Never generate INSERT, UPDATE, DELETE, DROP, or any mutating SQL.
            """)
    @UserMessage(
            """
            Schema:
            {schemaJson}

            Question: {question}
            """)
    AiSqlResponse generateSql(String schemaJson, String question);

    @SystemMessage(
            """
            You are given a user's question and the exact SQL result.
            Summarise the result accurately.
            Return ONLY valid JSON:
               {
                   "resultSnapshot": "<human-readable summary>"
               }
            Rules:
               - If the result is a single value, present just the number.
               - If the result contains multiple rows, include the values as a comma-separated list within the summary string.
               - If the result is empty, say "No matching records found."
               - Do not invent or infer information.
            """)
    @UserMessage(
            """
            Question: {question}
            SQL Result = {result}
            """)
    AiResultSnapshot generateResultSnapshot(String question, String result);
}
