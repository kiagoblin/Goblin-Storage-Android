# Goblin Link

Мод для Mindustry (ПК и Android): хранилище **Гоблин Линк**, которое связано с ядром и автоматически забирает предметы из него и возвращает обратно. Ёмкость и скорость растут с помощью модулей, которые работают на энергии.

A Mindustry mod (PC and Android): the **Goblin Link** storage connected to your core that automatically pulls items from it and returns them. Capacity and speed grow with power-hungry modules.

## Скачать / Download

- Последняя сборка / latest build: https://github.com/kiagoblin/Goblin-Storage-Android/releases/download/latest/Goblin-Storage-Android.jar
- Все версии / all versions: [Releases](https://github.com/kiagoblin/Goblin-Storage-Android/releases)
- Или найдите **Goblin Link** в браузере модов внутри игры. / Or find **Goblin Link** in the in-game mod browser.

Установка: Моды -> Импорт мода (или положите `.jar` в папку модов). / Install: Mods -> Import mod.

## Блоки

| Блок | Что делает |
|---|---|
| Гоблин Линк (2x2) | Забирает выбранные предметы из ядра и отдаёт соседним блокам; без выбора возвращает предметы в ядро. Без модулей: 10 предметов каждого вида и 2 предмета в секунду. |
| Гоблин-хранилище | Увеличивает ёмкость Линка. Работает на энергии. |
| Гоблин-процессор | Увеличивает скорость передачи. Работает на энергии. |
| Гоблин-узел | Большой радиус связи: к узлу привязываются модули, а узел привязывается к Линку. |

У модулей и узлов три стадии игры: ранняя (1x1), средняя (2x2) и поздняя (3x3). Блоки поздней стадии дополнительно требуют криогенную жидкость.

**Связывание:** нажмите на модуль, затем на Линк или узел в радиусе. Повторное нажатие разрывает связь. Нажав на узел, можно привязать к нему Линк и модули.

## Сборка

Мод собирается через GitHub Actions при каждом пуше в `master` (`./gradlew deploy`, Android-совместимый `.jar`).
