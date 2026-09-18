package io.virinchi.dhammanature.model.enums;

/**
 * Moderation state of a user-submitted blog article.
 * Posts start life as {@code PENDING} and are only shown publicly once an
 * administrator approves them.
 */
public enum BlogStatus {
    PENDING,
    APPROVED,
    REJECTED
}