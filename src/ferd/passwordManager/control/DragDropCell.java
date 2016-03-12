package ferd.passwordManager.control;

import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class DragDropCell<T> extends ListCell<T> {
    private static final DataFormat FORMAT = new DataFormat("dragDropDataFormat");

    public DragDropCell() {
        final ListCell<?> thisCell = this;

        this.setOnDragDetected(event -> {
            if (this.getItem() == null) {
                return;
            }

            final Dragboard dragboard = this.startDragAndDrop(TransferMode.MOVE);
            final ClipboardContent content = new ClipboardContent();
            content.put(FORMAT, this.getItem());
            dragboard.setContent(content);

            event.consume();
        });

        this.setOnDragOver(event -> {
            if (event.getGestureSource() != thisCell && event.getDragboard().hasContent(FORMAT)) {
                event.acceptTransferModes(TransferMode.MOVE);
            }

            event.consume();
        });

        this.setOnDragEntered(event -> {
            if (event.getGestureSource() != thisCell && event.getDragboard().hasContent(FORMAT)) {
                this.setOpacity(0.3);
            }
        });

        this.setOnDragExited(event -> {
            if (event.getGestureSource() != thisCell && event.getDragboard().hasContent(FORMAT)) {
                this.setOpacity(1);
            }
        });

        this.setOnDragDropped(event -> {
            if (this.getItem() == null) {
                return;
            }

            final Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasContent(FORMAT)) {
                final ObservableList<T> items = this.getListView().getItems();

                @SuppressWarnings("unchecked")
                final T item = (T) db.getContent(FORMAT);
                final int draggedIdx = items.indexOf(item);
                final int thisIdx = items.indexOf(this.getItem());

                if (thisIdx < draggedIdx) {
                    items.remove(draggedIdx);
                    items.add(thisIdx, item);
                } else {
                    items.add(thisIdx + 1, item);
                    items.remove(draggedIdx);
                }

                final ObservableList<T> l = this.getListView().getItems();
                this.getListView().setItems(null);
                this.getListView().setItems(l);
                this.getListView().getSelectionModel().clearSelection();
                this.getListView().getSelectionModel().select(item);

                success = true;
            }
            event.setDropCompleted(success);

            event.consume();
        });

        this.setOnDragDone(DragEvent::consume);
    }
}
