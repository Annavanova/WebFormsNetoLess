## WebFormsNetoLess
### Домашнее задание к занятию «1.2. Формы и форматы передачи данных»
#### Легенда
В рамках изучения Java Core и работы протокола HTTP вы использовали библиотеку HttpClient из состава Apache HttpComponents.

В состав этой библиотеки входит утилитный класс URLEncodedUtils, который и позволяет «парсить» Query String, извлекая параметры.

Необходимо добавить в ваш сервер из предыдущего ДЗ функциональность обработки параметров запроса так, чтобы можно было из объекта типа Request отдельно получать и путь запроса, и параметры из Query String.

Например, это можно сделать в виде метода getQueryParam(String name) и getQueryParams(). Подумайте, что они должны возвращать, исходя из документации на утилитный класс.

#### Задача
1. Подключите к своему проекту HttpClient.
2. Реализуйте функциональность по обработке параметров из Query.
3. При необходимости доработайте функциональность поиска хендлера так, чтобы учитывался только путь без Query, т. е. хендлер, зарегистрированный на "/messages", обрабатывал и запросы "/messages?last=10".
#### Результат
Реализуйте новую функциональность в ветке feature/query вашего репозитория из ДЗ 1 и откройте Pull Request.

Так как вы главный архитектор и проектировщик этого, уже более функционального решения, то все архитектурные решения принимать вам, поэтому будьте готовы к критике со стороны проверяющих.

В качестве решения пришлите ссылку на ваш Pull Request на GitHub в личном кабинете студента на сайте netology.ru.

После того, как домашнее задание будет принято, сделайте merge для Pull Request.
