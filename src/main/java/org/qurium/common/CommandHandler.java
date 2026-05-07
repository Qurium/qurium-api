/* Qurium - 2026 */
package org.qurium.common;

public interface CommandHandler<T extends Command, R> {

    R handle(T command);
}
