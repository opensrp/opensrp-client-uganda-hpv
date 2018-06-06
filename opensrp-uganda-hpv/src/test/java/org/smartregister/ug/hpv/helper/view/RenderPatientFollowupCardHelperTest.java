package org.smartregister.ug.hpv.helper.view;

import android.view.View;
import android.widget.Button;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.ug.hpv.activity.BasePatientDetailActivity;
import org.smartregister.ug.hpv.activity.PatientDetailActivity;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class RenderPatientFollowupCardHelperTest {

    @Mock
    private CommonPersonObjectClient client;

    @Mock
    private VaccineRepository vaccineRepository;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private RenderPatientFollowupCardHelper followupCardHelper;

    @Before
    public void setUp() {

        BasePatientDetailActivity activity = Robolectric.buildActivity(PatientDetailActivity.class).get();
        followupCardHelper = new RenderPatientFollowupCardHelper(activity, client);

        Mockito.when(client.entityId()).thenReturn("noString");
    }

    @Test
    public void testIsValidForUndoShouldReturnFalseIfHpv2IsSynced() {

        try {
            ArrayList<Vaccine> vaccines = generateVaccines(2, "Unsynced", "Synced");

            followupCardHelper.setVaccineRepository(vaccineRepository);
            Mockito.when(vaccineRepository.findByEntityId(anyString())).thenReturn(vaccines);

            boolean result = Whitebox.invokeMethod(followupCardHelper, "isValidForUndo");
            assertFalse(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIsValidForUndoShouldReturnTrueIfHpv1AndHpv2AreNotSynced() {

        try {
            ArrayList<Vaccine> vaccines = generateVaccines(2, "Unsynced", "Unsynced");

            followupCardHelper.setVaccineRepository(vaccineRepository);
            Mockito.when(vaccineRepository.findByEntityId(anyString())).thenReturn(vaccines);

            boolean result = Whitebox.invokeMethod(followupCardHelper, "isValidForUndo");
            assertTrue(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testIsValidForUndoShouldReturnTrueIfHpv1IsSyncedAndHpv2IsNotSynced() {

        try {
            ArrayList<Vaccine> vaccines = generateVaccines(2, "Synced", "Unsynced");

            followupCardHelper.setVaccineRepository(vaccineRepository);
            Mockito.when(vaccineRepository.findByEntityId(anyString())).thenReturn(vaccines);

            boolean result = Whitebox.invokeMethod(followupCardHelper, "isValidForUndo");
            assertTrue(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIsValidForUndoShouldReturnTrueIfHpv1IsNotSyncedAndHpv2DoesNotExist() {

        try {
            ArrayList<Vaccine> vaccines = generateVaccines(1, "Unsynced", null);

            followupCardHelper.setVaccineRepository(vaccineRepository);
            Mockito.when(vaccineRepository.findByEntityId(anyString())).thenReturn(vaccines);

            boolean result = Whitebox.invokeMethod(followupCardHelper, "isValidForUndo");
            assertTrue(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIsValidForUndoShouldReturnFalseIfHpv1IsSyncedAndHpv2DoesNotExist() {

        try {
            ArrayList<Vaccine> vaccines = generateVaccines(1, "Synced", null);

            followupCardHelper.setVaccineRepository(vaccineRepository);
            Mockito.when(vaccineRepository.findByEntityId(anyString())).thenReturn(vaccines);

            boolean result = Whitebox.invokeMethod(followupCardHelper, "isValidForUndo");
            assertFalse(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRenderUndoVaccinationButtonShouldRenderButtonIfActivateIsTrue() {

        ArrayList<Vaccine> vaccines = generateVaccines(2, "Unsynced", "Unsynced");

        Mockito.when(vaccineRepository.findByEntityId(anyString())).thenReturn(vaccines);
        followupCardHelper.setVaccineRepository(vaccineRepository);

        Button undoBtn = new Button(RuntimeEnvironment.application);
        undoBtn.setVisibility(View.GONE);

        try {
            Whitebox.invokeMethod(followupCardHelper, "renderUndoVaccinationButton", true, undoBtn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(undoBtn.getVisibility(), View.VISIBLE);
    }

    @Test
    public void testRenderUndoVaccinationButtonShouldNotRenderButtonIfActivateIsFalse() {

        ArrayList<Vaccine> vaccines = generateVaccines(2, "Unsynced", "Unsynced");

        Mockito.when(vaccineRepository.findByEntityId(anyString())).thenReturn(vaccines);
        followupCardHelper.setVaccineRepository(vaccineRepository);

        Button undoBtn = new Button(RuntimeEnvironment.application);
        undoBtn.setVisibility(View.VISIBLE);

        try {
             Whitebox.invokeMethod(followupCardHelper, "renderUndoVaccinationButton", false, undoBtn);
        } catch (Exception e) {
             e.printStackTrace();
        }
        assertEquals(undoBtn.getVisibility(), View.GONE);
    }

    private ArrayList<Vaccine> generateVaccines(int numDoses, String hpv1Status, String hpv2Status) {

        ArrayList vaccines = new ArrayList();
        for (int i = 1; i < numDoses + 1; i++) {

            Vaccine vaccine = new Vaccine();
            vaccine.setName("hpv " + i);
            if (i == 1) {
                vaccine.setSyncStatus(hpv1Status);
            } else {
                vaccine.setSyncStatus(hpv2Status);
            }
            vaccines.add(vaccine);
        }
        return vaccines;
    }
}
