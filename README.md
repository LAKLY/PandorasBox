# 📦 Pandora's Box — Документация

**Версия мода:** 1.0.0  
**Для Minecraft:** 1.21.1  
**Загрузчик:** NeoForge 21.1.72+  

---

## 1. О моде

**Pandora's Box** — это сюжетный мод, добавляющий мистический предмет «Ящик Пандоры». Это не просто инструмент, а интерактивный спутник, который:

- Общается с игроком через систему диалогов (как субтитры)
- Выдаёт квесты и отслеживает их выполнение
- Комментирует действия игрока (смерть, добыча блоков, убийство мобов)
- Ведёт игрока по сюжетной линии
- Полностью настраивается через JSON-конфиги без изменения кода

Мод вдохновлён **Enigmatic Legacy** и предоставляет создателям модпаков мощный инструмент для создания нарративных приключений.

---

## 2. Установка

1. Скачайте мод и поместите его в папку `mods` вашего клиента/сервера NeoForge.
2. При первом запуске мод автоматически сгенерирует папку `config/pandoras_box/` с примерами конфигов (если они не были созданы ранее).
3. Перезапустите игру или сервер.

---

## 3. Структура папок и файлов

```
config/pandoras_box/
├── settings.json               # Основные настройки мода
├── random_phrases.json         # Список случайных фраз (ПКМ по ящику)
├── quests/                     # Папка с квестами
│   ├── awakening.json          # Пример квеста
│   └── first_trial.json        # Пример квеста
├── dialogues/                  # Папка с диалогами (деревья)
│   ├── introduction.json
│   ├── awakening_complete.json
│   ├── first_trial_complete.json
│   ├── death_comment_1.json
│   └── ... (другие диалоги)
└── events/                     # Папка с комментариями на действия
    └── player_actions.json
```

Все файлы — в формате JSON.

---

## 4. Основные настройки (`settings.json`)

```json
{
  "initialQuest": "awakening",                     // ID стартового квеста
  "initialDialogue": "introduction",              // (устарело, используется startDialogue в квесте)
  "deathDialogues": [                              // Список диалогов при смерти игрока
    "death_comment_1",
    "death_comment_2",
    "death_comment_3"
  ]
}
```

Если файл отсутствует, используются значения по умолчанию.

---

## 5. Случайные фразы (`random_phrases.json`)

При обычном ПКМ по ящику (без Shift) воспроизводится случайная фраза из этого списка. Если диалог уже активен, фраза не воспроизводится (защита от прерывания).

```json
{
  "phrases": [
    "death_comment_1",
    "death_comment_2",
    "death_comment_3"
  ]
}
```

ID фраз должны соответствовать существующим деревьям диалогов в папке `dialogues/`.

---

## 6. Квесты (`quests/*.json`)

### Формат

```json
{
  "questId": "awakening",                          // Уникальный ID квеста
  "title": "Awakening",                            // Название
  "description": "You open your eyes...",          // Описание
  "repeatable": false,                             // Можно ли проходить повторно
  "startDialogue": "introduction",                 // (опционально) Диалог при старте квеста
  "onCompletionDialogue": "awakening_complete",    // Диалог при завершении
  "nextQuestId": "first_trial",                    // Следующий квест в цепочке
  "tasks": [                                       // Список задач
    {
      "taskId": "listen_to_box",                   // Уникальный ID задачи
      "type": "dialogue_choice",                   // Тип задачи
      "description": "Listen to Pandora...",       // Описание
      "requiredChoice": -1,                        // Специфичные для типа параметры
      "required": 1                                // Количество для выполнения
    },
    {
      "taskId": "gather_wood",
      "type": "break_block",
      "description": "Gather 5 Oak Logs",
      "blockType": "minecraft:oak_log",
      "required": 5
    }
  ],
  "rewards": [                                     // Награды за выполнение
    {
      "rewardId": "starter_xp",
      "type": "experience",
      "amount": 50
    }
  ]
}
```

### Типы задач (type)

| Тип                 | Описание                                                | Дополнительные поля                           |
|---------------------|---------------------------------------------------------|-----------------------------------------------|
| `kill_entity`       | Убить N мобов определённого типа                        | `entityType` (например `minecraft:zombie`)   |
| `break_block`       | Сломать N блоков определённого типа                     | `blockType` (например `minecraft:oak_log`)   |
| `collect_item`      | Подобрать N предметов                                   | `itemType` (например `minecraft:diamond`)    |
| `craft_item`        | Скрафтить N предметов                                   | `itemType`                                    |
| `use_item`          | Использовать предмет N раз (ПКМ)                        | `itemType`                                    |
| `reach_level`       | Достичь определённого уровня опыта                      | `targetLevel` (число)                         |
| `interact_block`    | Взаимодействовать с блоком N раз (ПКМ по блоку)         | `blockType`                                   |
| `location`          | Достичь указанных координат                             | `locationX`, `locationY`, `locationZ`, `radius` |
| `dialogue_choice`   | Сделать выбор в диалоге (отмечается через `onSelectQuestTaskId`) | `requiredChoice` (ID выбора или -1 для любого) |

### Награды (rewards)

| Тип           | Описание                          | Поля                         |
|---------------|-----------------------------------|------------------------------|
| `experience`  | Выдать опыт                       | `amount`                     |
| `item`        | Выдать предмет                    | `itemId`, `count` (в разработке) |
| `permission`  | Выдать разрешение (внутренний флаг) | `permissionId`               |

---

## 7. Диалоги (`dialogues/*.json`)

Диалог — это дерево узлов (nodes). Каждый узел может содержать сообщение и/или варианты выбора.

### Формат

```json
{
  "treeId": "introduction",
  "startNode": "start",
  "nodes": {
    "start": {
      "id": "start",
      "speaker": "Narrator",
      "text": "You slowly regain consciousness...",
      "duration": 3,                    // Время показа в секундах
      "type": "NARRATOR",               // AMBIENT / NARRATOR / ACTION (влияет на цвет)
      "nextNode": "pandora_intro"       // Следующий узел (если нет выбора)
    },
    "pandora_purpose": {
      "id": "pandora_purpose",
      "speaker": "Pandora",
      "text": "I am Pandora's Box...",
      "duration": 4,
      "type": "AMBIENT",
      "choices": [                      // Список вариантов ответа
        {
          "id": 0,
          "text": "Who are you?",
          "nextNode": "response_curious",
          "onSelectQuestTaskId": "listen_to_box"   // ID задачи, которую отметить выполненной при выборе
        },
        {
          "id": 1,
          "text": "This is a dream.",
          "nextNode": "response_skeptical",
          "onSelectQuestTaskId": "listen_to_box"
        }
      ]
    },
    "response_curious": {
      "id": "response_curious",
      "speaker": "Pandora",
      "text": "Curiosity. Good...",
      "duration": 4,
      "type": "AMBIENT",
      "nextNode": "end"
    },
    "end": {
      "id": "end",
      "speaker": "Pandora",
      "text": "Now, go...",
      "duration": 3,
      "type": "AMBIENT",
      "nextNode": null                   // null означает конец диалога
    }
  }
}
```

### Поля узла

| Поле        | Тип                      | Обязательное | Описание                              |
|-------------|--------------------------|--------------|---------------------------------------|
| `id`        | string                   | Да           | Уникальный ID узла                    |
| `speaker`   | string                   | Да           | Имя говорящего                        |
| `text`      | string                   | Да           | Текст сообщения                       |
| `duration`  | integer                  | Нет (по умолч. 4) | Время показа в секундах            |
| `type`      | `AMBIENT` / `NARRATOR` / `ACTION` | Нет (AMBIENT) | Тип сообщения (влияет на цвет) |
| `nextNode`  | string или null          | Нет           | Следующий узел (если нет выбора)      |
| `choices`   | массив объектов          | Нет           | Варианты ответа                       |

### Поля выбора (choice)

| Поле                    | Тип    | Описание                                      |
|-------------------------|--------|-----------------------------------------------|
| `id`                    | int    | Уникальный ID выбора (в пределах узла)        |
| `text`                  | string | Текст на кнопке                               |
| `nextNode`              | string | Узел, на который перейти после выбора         |
| `onSelectQuestTaskId`   | string | ID задачи (типа `dialogue_choice`), которую нужно отметить выполненной |

---

## 8. События-комментарии (`events/*.json`)

Эти файлы определяют, когда Пандора должна комментировать действия игрока.

```json
{
  "events": [
    {
      "eventId": "first_stone_break",
      "triggerType": "block_broken",
      "blockType": "minecraft:stone",
      "dialogueId": "first_stone_comment",
      "isOneTime": true
    }
  ]
}
```

### Поля

| Поле           | Тип              | Описание                                  |
|----------------|------------------|-------------------------------------------|
| `eventId`      | string           | Уникальный ID события                     |
| `triggerType`  | `block_broken` / `entity_killed` / `player_death` / `dimension_change` | Тип триггера |
| `blockType` / `entityType` / `dimension` | string | Значение для сопоставления |
| `dialogueId`   | string           | ID диалога (дерева), который запустить    |
| `isOneTime`    | boolean          | Если true, сработает только один раз      |

---

## 9. API для разработчиков модпаков

Класс `PandorasBoxAPI` предоставляет методы для программного управления модом (без изменения кода самого мода).

```java
// Получить экземпляр API
PandorasBoxAPI api = PandorasBoxAPI.get();

// Регистрация квеста
api.registerQuest(quest);

// Регистрация цепочки квестов
api.registerQuestChain(chain);

// Начать квест для игрока
api.giveQuestToPlayer(player, "awakening");

// Завершить квест
api.completeQuestForPlayer(player, "awakening");

// Запустить диалог
api.playDialogue(player, "introduction");

// Обработать выбор в диалоге
api.onDialogueChoiceMade(player, "introduction", 0);

// Зарегистрировать комментарий на действие
api.addActionCommentary("custom_event", "block_broken", "minecraft:stone", "stone_dialogue", true);

// Выдать/проверить разрешение
api.grantPermission(player, "received_pandora_box");
boolean hasPerm = api.hasPermission(player, "received_pandora_box");

// Зарегистрировать новый тип задачи (для расширения)
api.registerTaskType("custom_task", CustomTask::new);

// Зарегистрировать новый тип награды
api.registerRewardType("custom_reward", CustomReward::new);
```

### Добавление новых типов задач (для программистов)

Чтобы добавить свой тип задачи, создайте класс, наследующий `QuestTask`, и зарегистрируйте его через API или в `ConfigLoader`.

```java
public class MyTask extends QuestTask {
    @Override
    public void onEvent(Event event) {
        // Логика отслеживания
    }
}

// Регистрация
PandorasBoxAPI.get().registerTaskType("my_task", MyTask::new);
```

---

## 10. GUI журнала квестов

- **Открытие:** Shift + ПКМ по Ящику Пандоры.
- Отображает все активные квесты.
- Для каждого квеста показывает название, описание, прогресс выполнения и список задач с индикацией выполнения (зелёный — выполнено, красный — нет).
- Поддерживает прокрутку, если квестов много.

---

**© 2026, Pandora's Box Mod**  
Разработано для NeoForge 1.21.1.  
Лицензия: MIT (см. файл LICENSE).
