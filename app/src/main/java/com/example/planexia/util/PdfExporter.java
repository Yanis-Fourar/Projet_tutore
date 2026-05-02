package com.example.planexia.util;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import com.example.planexia.model.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PdfExporter {

    private static final int PAGE_W = 595;
    private static final int PAGE_H = 842;
    private static final int MARGIN = 40;

    public static Uri export(Context context, List<Task> tasks) throws Exception {
        PdfDocument doc = buildDocument(tasks);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, "planexia_taches.pdf");
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri itemUri = context.getContentResolver().insert(collection, values);
            if (itemUri == null) throw new Exception("Impossible de créer le fichier dans Téléchargements");

            try (OutputStream out = context.getContentResolver().openOutputStream(itemUri)) {
                doc.writeTo(out);
            }
            values.clear();
            values.put(MediaStore.Downloads.IS_PENDING, 0);
            context.getContentResolver().update(itemUri, values, null, null);
            doc.close();
            return itemUri;
        } else {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(dir, "planexia_taches.pdf");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                doc.writeTo(fos);
            }
            doc.close();
            return FileProvider.getUriForFile(context, "com.example.planexia.fileprovider", file);
        }
    }

    private static PdfDocument buildDocument(List<Task> tasks) {
        PdfDocument doc = new PdfDocument();

        Map<String, List<Task>> byModule = new LinkedHashMap<>();
        for (Task t : tasks) {
            String mod = t.getModuleName().isEmpty() ? "Sans module" : t.getModuleName();
            byModule.computeIfAbsent(mod, k -> new ArrayList<>()).add(t);
        }

        Paint titlePaint  = makePaint("#6C3FC5", 20f, true);
        Paint metaPaint   = makePaint("#888888", 11f, false);
        Paint modPaint    = makePaint("#1A1A2E", 13f, true);
        Paint tTaskPaint  = makePaint("#1A1A2E", 12f, true);
        Paint detailPaint = makePaint("#666666", 11f, false);
        Paint donePaint   = makePaint("#4CAF50", 11f, false);
        Paint todoPaint   = makePaint("#F5A623", 11f, false);
        Paint latePaint   = makePaint("#E53935", 11f, false);
        Paint linePaint   = makePaint("#E0E0E0", 1f, false);
        linePaint.setStrokeWidth(1f);
        Paint cardPaint   = makePaint("#F8F5FF", 12f, false);
        cardPaint.setStyle(Paint.Style.FILL);
        Paint barPaint    = makePaint("#6C3FC5", 12f, false);
        barPaint.setStyle(Paint.Style.FILL);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        int[] pageNum = {1};
        PdfDocument.Page[] page = {startPage(doc, pageNum[0])};
        Canvas[] cv = {page[0].getCanvas()};
        float[] y = {50f};

        // Header
        cv[0].drawText("Planexia — Export des tâches", MARGIN, y[0], titlePaint);
        y[0] += 18f;
        String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(new Date());
        cv[0].drawText("Généré le " + dateStr + "  •  " + tasks.size() + " tâche" + (tasks.size() > 1 ? "s" : ""), MARGIN, y[0], metaPaint);
        y[0] += 10f;
        cv[0].drawLine(MARGIN, y[0], PAGE_W - MARGIN, y[0], linePaint);
        y[0] += 20f;

        for (Map.Entry<String, List<Task>> entry : byModule.entrySet()) {
            String modName = entry.getKey();
            List<Task> modTasks = entry.getValue();

            if (y[0] + 30f + 65f > PAGE_H - 40f) {
                doc.finishPage(page[0]);
                pageNum[0]++;
                page[0] = startPage(doc, pageNum[0]);
                cv[0] = page[0].getCanvas();
                y[0] = 50f;
            }

            barPaint.setColor(moduleColor(modTasks));
            cv[0].drawRect(MARGIN, y[0] - 13f, MARGIN + 4f, y[0] + 3f, barPaint);
            cv[0].drawText(modName.toUpperCase(), MARGIN + 10f, y[0], modPaint);
            y[0] += 18f;

            for (Task t : modTasks) {
                if (y[0] + 65f > PAGE_H - 40f) {
                    doc.finishPage(page[0]);
                    pageNum[0]++;
                    page[0] = startPage(doc, pageNum[0]);
                    cv[0] = page[0].getCanvas();
                    y[0] = 50f;
                }

                RectF card = new RectF(MARGIN, y[0], PAGE_W - MARGIN, y[0] + 58f);
                cv[0].drawRoundRect(card, 8f, 8f, cardPaint);
                barPaint.setColor(moduleColor(modTasks));
                cv[0].drawRect(MARGIN, y[0], MARGIN + 4f, y[0] + 58f, barPaint);

                float tx = MARGIN + 14f;
                float ty = y[0] + 16f;

                cv[0].drawText(truncate(t.getTitle(), tTaskPaint, PAGE_W - MARGIN - tx - 70f), tx, ty, tTaskPaint);
                ty += 14f;

                // Ressource (remplace getObjectiveName — non disponible dans le modèle Task)
                if (t.getResourceText() != null && !t.getResourceText().isEmpty()) {
                    cv[0].drawText(truncate("Ressource : " + t.getResourceText(), detailPaint, PAGE_W - MARGIN - tx - 10f), tx, ty, detailPaint);
                }
                ty += 14f;

                String dateInfo = t.getDueDate() != null ? formatDate(t.getDueDate()) : "Sans date";
                cv[0].drawText("Échéance : " + dateInfo, tx, ty, detailPaint);

                boolean isLate = !t.isDone() && t.getDueDate() != null && t.getDueDate().compareTo(today) < 0;
                String status;
                Paint sPaint;
                if (t.isDone())      { status = "Terminée";  sPaint = donePaint; }
                else if (isLate)     { status = "En retard"; sPaint = latePaint; }
                else                 { status = "À faire";   sPaint = todoPaint; }

                float sx = PAGE_W - MARGIN - sPaint.measureText(status) - 6f;
                cv[0].drawText(status, sx, ty, sPaint);

                y[0] += 64f;
            }
            y[0] += 12f;
        }

        doc.finishPage(page[0]);
        return doc;
    }

    private static PdfDocument.Page startPage(PdfDocument doc, int num) {
        return doc.startPage(new PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, num).create());
    }

    private static Paint makePaint(String hex, float size, boolean bold) {
        Paint p = new Paint();
        p.setColor(Color.parseColor(hex));
        p.setTextSize(size);
        p.setFakeBoldText(bold);
        p.setAntiAlias(true);
        return p;
    }

    private static int moduleColor(List<Task> tasks) {
        if (!tasks.isEmpty()) {
            try { return Color.parseColor(tasks.get(0).getModuleColor()); } catch (Exception ignored) {}
        }
        return Color.parseColor("#6C3FC5");
    }

    private static String truncate(String text, Paint paint, float maxWidth) {
        if (paint.measureText(text) <= maxWidth) return text;
        while (text.length() > 1 && paint.measureText(text + "…") > maxWidth)
            text = text.substring(0, text.length() - 1);
        return text + "…";
    }

    private static String formatDate(String yyyyMMdd) {
        try {
            return new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
                    .format(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(yyyyMMdd));
        } catch (Exception e) { return yyyyMMdd; }
    }
}