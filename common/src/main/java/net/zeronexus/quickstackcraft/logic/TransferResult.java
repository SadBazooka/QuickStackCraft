package net.zeronexus.quickstackcraft.logic;

/**
 * Result of a quick stack or dump operation.
 */
public record TransferResult(int itemsMoved, int containersUsed) {

    public static final TransferResult EMPTY = new TransferResult(0, 0);

    public boolean didSomething() {
        return itemsMoved > 0;
    }
}
