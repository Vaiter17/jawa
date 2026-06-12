package datastructures;

import models.Patient;
import java.util.ArrayList;
import java.util.List;

public class MinHeapQueue {
    private List<Patient> heap;

    public MinHeapQueue() {
        heap = new ArrayList<>();
    }

    private int parent(int index) { return (index - 1) / 2; }
    private int leftChild(int index) { return (2 * index) + 1; }
    private int rightChild(int index) { return (2 * index) + 2; }

    private void swap(int index1, int index2) {
        Patient temp = heap.get(index1);
        heap.set(index1, heap.get(index2));
        heap.set(index2, temp);
    }

    public void insert(Patient patient) {
        heap.add(patient);
        heapifyUp(heap.size() - 1);
    }

    public Patient extractMin() {
        if (heap.isEmpty()) {
            return null;
        }
        if (heap.size() == 1) {
            return heap.remove(0);
        }

        Patient min = heap.get(0);
        heap.set(0, heap.remove(heap.size() - 1));
        heapifyDown(0);

        return min;
    }

    public Patient peek() {
        if (heap.isEmpty()) return null;
        return heap.get(0);
    }

    public boolean updatePriority(String patientId, int newScore) {
        for (int i = 0; i < heap.size(); i++) {
            if (heap.get(i).getPatientId().equals(patientId)) {
                int oldScore = heap.get(i).getFinalSeverityScore();
                heap.get(i).setFinalSeverityScore(newScore);

                if (newScore < oldScore) {
                    heapifyUp(i);
                } else {
                    heapifyDown(i);
                }
                return true;
            }
        }
        return false;
    }

    private void heapifyUp(int index) {
        int currentIndex = index;
        while (currentIndex > 0 && heap.get(currentIndex).compareTo(heap.get(parent(currentIndex))) < 0) {
            swap(currentIndex, parent(currentIndex));
            currentIndex = parent(currentIndex);
        }
    }

    private void heapifyDown(int index) {
        int minIndex = index;
        int left = leftChild(index);
        int right = rightChild(index);

        if (left < heap.size() && heap.get(left).compareTo(heap.get(minIndex)) < 0) {
            minIndex = left;
        }

        if (right < heap.size() && heap.get(right).compareTo(heap.get(minIndex)) < 0) {
            minIndex = right;
        }

        if (minIndex != index) {
            swap(index, minIndex);
            heapifyDown(minIndex);
        }
    }

    public List<Patient> getAllPatients() {
        return new ArrayList<>(heap);
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public String getHeapVisualization() {
        if (heap.isEmpty()) return "The heap is currently empty.";
        StringBuilder sb = new StringBuilder();
        sb.append("Min-Heap Array Representation:\n");
        for (int i = 0; i < heap.size(); i++) {
            Patient p = heap.get(i);
            String details = String.format("Score: %d | Base: %d, Mod: %d", p.getFinalSeverityScore(), p.getBaseSeverity(), p.getPainModifier());
            sb.append(String.format(" [%d] -> %s (%s)\n", i, p.getName(), details));
        }
        
        sb.append("\nPriority Score 1 is the most critical");
        sb.append("\nHigh pain (10/10) results in a negative modifier");
        sb.append("\nwhich lowers the Priority Score closer to 1 (making them treated faster).\n");
        
        sb.append("\nBinary Tree Structure (O(log N) heights):\n");
        buildTreeString(sb, 0, "", true);
        return sb.toString();
    }

    private void buildTreeString(StringBuilder sb, int index, String prefix, boolean isTail) {
        if (index >= heap.size()) return;
        
        Patient p = heap.get(index);
        String details = String.format("(Score: %d | Base: %d, Mod: %d)", p.getFinalSeverityScore(), p.getBaseSeverity(), p.getPainModifier());
        
        sb.append(prefix).append(isTail ? "\\-- " : "|-- ").append(p.getName())
          .append(" ").append(details).append("\n");
          
        int left = leftChild(index);
        int right = rightChild(index);
        
        boolean hasLeft = left < heap.size();
        boolean hasRight = right < heap.size();
        
        if (hasLeft) {
            buildTreeString(sb, left, prefix + (isTail ? "    " : "|   "), !hasRight);
        }
        if (hasRight) {
            buildTreeString(sb, right, prefix + (isTail ? "    " : "|   "), true);
        }
    }
}
