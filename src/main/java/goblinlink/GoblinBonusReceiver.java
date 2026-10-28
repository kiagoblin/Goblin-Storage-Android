package goblinlink;

/**
 * Приёмник бонусов от модулей: Линк или узел связи.
 * Модули вызывают addBonus каждый тик, пока работают и подключены.
 */
public interface GoblinBonusReceiver {

    /** Добавляет бонус к ёмкости (на вид предмета) и к скорости передачи (предметов в секунду) на этот тик. */
    void addBonus(int capacity, float speed);
}
