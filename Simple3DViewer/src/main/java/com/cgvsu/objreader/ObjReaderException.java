package com.cgvsu.objreader;

 // NOTE: Назначение: исключение парсера OBJ с привязкой к номеру строки.
 
public class ObjReaderException extends RuntimeException {
    public ObjReaderException(String errorMessage, int lineInd) {
        super("Error parsing OBJ file on line: " + lineInd + ". " + errorMessage);
    }
}
