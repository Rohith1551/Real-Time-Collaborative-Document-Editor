
package com.example.Real_Time_Collaborative_Document_Editor;

import com.example.Real_Time_Collaborative_Document_Editor.dto.DocumentOperationTransformData;
import com.example.Real_Time_Collaborative_Document_Editor.entity.Document;
import com.example.Real_Time_Collaborative_Document_Editor.entity.DocumentPermission;
import com.example.Real_Time_Collaborative_Document_Editor.entity.User;
import com.example.Real_Time_Collaborative_Document_Editor.enums.Role;
import com.example.Real_Time_Collaborative_Document_Editor.kafka.DocumentEventProducer;
import com.example.Real_Time_Collaborative_Document_Editor.repository.DocumentOperationRepository;
import com.example.Real_Time_Collaborative_Document_Editor.repository.DocumentPermissionRepository;
import com.example.Real_Time_Collaborative_Document_Editor.repository.DocumentRepository;
import com.example.Real_Time_Collaborative_Document_Editor.repository.DocumentVersionRepository;
import com.example.Real_Time_Collaborative_Document_Editor.repository.UserRepository;
import com.example.Real_Time_Collaborative_Document_Editor.service.DocumentService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentrepo;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DocumentVersionRepository documentVersionRepository;

    @Mock
    private DocumentPermissionRepository documentpermissionRepo;

    @Mock
    private DocumentOperationRepository documentOperationRepository;

    @Mock
    private DocumentEventProducer documentEventProducer;

    @InjectMocks
    private DocumentService documentService;

    // ---------------- TEST HELPERS ----------------

    private Integer transformInsertPosition(
            Integer incomingPosition,
            Integer previousPosition,
            String previousText
    ) throws Exception {

        Method method = DocumentService.class.getDeclaredMethod(
                "transformInsertPosition",
                Integer.class,
                Integer.class,
                String.class
        );

        method.setAccessible(true);

        return (Integer) method.invoke(
                documentService,
                incomingPosition,
                previousPosition,
                previousText
        );
    }

    private List<String> transformDeleteOperation(
            Integer incomingPosition,
            String incomingText,
            Integer previousPosition,
            String previousText
    ) throws Exception {

        Method method = DocumentService.class.getDeclaredMethod(
                "transformDeleteOperation",
                Integer.class,
                String.class,
                Integer.class,
                String.class
        );

        method.setAccessible(true);

        Object transformation = method.invoke(
                documentService,
                incomingPosition,
                incomingText,
                previousPosition,
                previousText
        );

        Method getRangesMethod =
                transformation.getClass().getDeclaredMethod("getRanges");

        getRangesMethod.setAccessible(true);

        List<?> ranges =
                (List<?>) getRangesMethod.invoke(transformation);

        List<String> results = new ArrayList<>();

        for (Object range : ranges) {
            Method getPositionMethod =
                    range.getClass().getDeclaredMethod("getPosition");

            Method getTextMethod =
                    range.getClass().getDeclaredMethod("getText");

            getPositionMethod.setAccessible(true);
            getTextMethod.setAccessible(true);

            Integer position =
                    (Integer) getPositionMethod.invoke(range);

            String text =
                    (String) getTextMethod.invoke(range);

            results.add(position + ":" + text);
        }

        return results;
    }

    // ---------------- INSERT TRANSFORMATION TESTS ----------------

    @Test
    void shouldTransformInsertAgainstPreviousInsert() throws Exception {
        assertEquals(8, transformInsertPosition(5, 3, "ABC"));
    }

    @Test
    void shouldNotShiftPositionWhenPreviousInsertIsAfter()
            throws Exception {
        assertEquals(5, transformInsertPosition(5, 7, "ABC"));
    }

    @Test
    void shouldShiftPositionWhenBothInsertsAreAtSamePosition()
            throws Exception {
        assertEquals(7, transformInsertPosition(5, 5, "AB"));
    }

    @Test
    void shouldHandleInsertAtBeginningOfDocument() throws Exception {
        assertEquals(1, transformInsertPosition(0, 0, "X"));
    }

    @Test
    void shouldHandleInsertAfterPreviousInsertAtBeginning()
            throws Exception {
        assertEquals(5, transformInsertPosition(3, 0, "AB"));
    }

    // ---------------- DELETE TRANSFORMATION TESTS ----------------

    @Test
    void shouldShiftDeleteWhenPreviousDeletionIsBefore()
            throws Exception {
        assertEquals(
                List.of("3:ABC"),
                transformDeleteOperation(5, "ABC", 1, "XY")
        );
    }

    @Test
    void shouldNotShiftDeleteWhenPreviousDeletionIsAfter()
            throws Exception {
        assertEquals(
                List.of("5:ABC"),
                transformDeleteOperation(5, "ABC", 9, "XY")
        );
    }

    @Test
    void shouldReturnNoRangesWhenPreviousDeletionCoversIncomingDelete()
            throws Exception {
        assertEquals(
                List.of(),
                transformDeleteOperation(5, "ABC", 3, "ABCDE")
        );
    }

    @Test
    void shouldReturnNoRangesWhenBothDeletionsAreIdentical()
            throws Exception {
        assertEquals(
                List.of(),
                transformDeleteOperation(2, "ABC", 2, "ABC")
        );
    }

    @Test
    void shouldSplitIncomingDeleteWhenPreviousDeletionIsInsideIt()
            throws Exception {
        assertEquals(
                List.of("2:AB", "4:EF"),
                transformDeleteOperation(2, "ABCDEF", 4, "XY")
        );
    }

    @Test
    void shouldHandleOverlapAtBeginningOfIncomingDelete()
            throws Exception {
        assertEquals(
                List.of("0:CDE"),
                transformDeleteOperation(2, "ABCDE", 0, "WXYZ")
        );
    }

    @Test
    void shouldHandleOverlapAtEndOfIncomingDelete()
            throws Exception {
        assertEquals(
                List.of("2:ABC"),
                transformDeleteOperation(2, "ABCDE", 5, "WXYZ")
        );
    }

    @Test
    void shouldHandleAdjacentPreviousDeletionBeforeIncomingDelete()
            throws Exception {
        assertEquals(
                List.of("3:ABC"),
                transformDeleteOperation(5, "ABC", 3, "XY")
        );
    }

    @Test
    void shouldHandleAdjacentPreviousDeletionAfterIncomingDelete()
            throws Exception {
        assertEquals(
                List.of("5:ABC"),
                transformDeleteOperation(5, "ABC", 8, "XY")
        );
    }

    @Test
    void shouldReturnNoRangesWhenIncomingDeleteTextIsEmpty()
            throws Exception {
        assertEquals(
                List.of(),
                transformDeleteOperation(5, "", 3, "XY")
        );
    }

    @Test
    void shouldReturnNoRangesWhenIncomingDeleteTextIsNull()
            throws Exception {
        assertEquals(
                List.of(),
                transformDeleteOperation(5, null, 3, "XY")
        );
    }

    // ---------------- REGRESSION TESTS ----------------

    @Test
    void shouldPreserveInsertedTextWhenDeletingAcrossIt() {

        Document document = mock(Document.class);
        User user = mock(User.class);
        DocumentPermission permission = mock(DocumentPermission.class);

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(10L);

        when(documentpermissionRepo.findByDocumentIdAndUserId(1L, 10L))
                .thenReturn(Optional.of(permission));

        when(permission.getRole()).thenReturn(Role.EDITOR);

        when(documentrepo.findById(1L))
                .thenReturn(Optional.of(document));

        when(document.getId()).thenReturn(1L);
        when(document.getContent()).thenReturn("abcXdef");
        when(document.getTitle()).thenReturn("Test Document");
        when(document.getVersion()).thenReturn(1L);

        when(documentrepo.saveAndFlush(document))
                .thenReturn(document);

        DocumentOperationTransformData previousOperation =
                new DocumentOperationTransformData(
                        "INSERT", 3, "X", 1L
                );

        when(documentOperationRepository.findOperationsForTransformation(
                1L, 0L
        )).thenReturn(List.of(previousOperation));

        assertDoesNotThrow(() ->
                documentService.applyOperation(
                        1L,
                        "DELETE",
                        2,
                        "cde",
                        0L,
                        "user@test.com"
                )
        );

        verify(document).setContent("abXf");
    }
}
